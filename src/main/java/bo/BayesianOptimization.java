package bo;

import java.util.Random;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.ejml.simple.SimpleMatrix;

import sobol.Sobol;
import design.LatinHypercubeSampling;
import functions.Function;
import gp.GPOptions;
import gp.GaussianProcess;
import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;

public class BayesianOptimization {

	private double fmin = Double.POSITIVE_INFINITY;
	private double [] fopt;
	private GaussianProcess gp;

	public BayesianOptimization()
	{
	}

	/**
	 * Find the minimum of function f using Bayesian optimization method based on Gaussian process
	 * Uses Latin hypercube sampling to find the initial n0 design points for fitting the Gaussian process
	 * @param random
	 * @param f - the function to find the minimum
	 * @param gp - GP with fixed Kernel parameter to be used for modeling f 
	 * @param n0 - initial design points to generate
	 * @param maxIter - maximum number of iterations or budget
	 */
	public void minimize(Random random, Function f, int n0, int maxIter) {

		// The standard approach is as follows:
		// 1. generate initial design points, D and evaluate f at D, i.e., y = f(D)
		// 2. batch fit GP on these initial design points
		// 3. maximize EI criterion to find the next point, x,  to evaluate
		// 4. evaluate f(x), add (x, f(x)) to the training data, D' = D + (x, f(x))
		// 5. re-fit GP on D'
		// 6. repeat until the budget is reached or the algorithm converged

		double [][] X = LatinHypercubeSampling.sample(random, n0, f.getDim());
		SimpleMatrix xMat = new SimpleMatrix(X);
		double [] y = f.value(xMat);

		Kernel kernel = new SquaredExponential(f.getDim());
		
		SearchInterval bounds = new SearchInterval(GPOptions.searchLowerBound, GPOptions.searchUpperBound);
		gp = GaussianProcess.fitParameters(random, kernel, X, y, bounds);
		System.out.println("loglik for GP fit: " + gp.getLogLik());
		
		for (int i = 0; i < y.length; i++) {
			if (y[i] < fmin) {
				fmin = y[i];
				fopt = X[i];
			}
		}
		
  		System.out.print("Current min=" + fmin + " attained at (" );
  		for (int i = 0; i < fopt.length; i++) {
  			System.out.print(fopt[i]);
  			if (i < (fopt.length - 1))
  				System.out.print(", ");
  		}
  		System.out.println(")");


		Function gpPredict = new Function() {

			@Override
			public double[] value(SimpleMatrix xx) {
				int n = xx.numRows();
				double [] ret = new double[n];
				double [] muVar = new double[2];
				for (int i = 0; i < n; i++) {
					muVar = gp.predict(xx.extractVector(true, i).getMatrix().getData());
					ret[i] = muVar[0];
				}
				return ret;
			}
			
			@Override
			public double value(double[] x) {
				return gp.predict(x)[0];
			}
			
			@Override
			public int getDim() {
				return gp.getDim();
			}
		};
		
		ExpectedImprovement maxEI = new ExpectedImprovement();

		double currEI = Double.POSITIVE_INFINITY;
		int currIter = 0;
		int numCriterionMet = 0;
		int gpRefitCounter = 0;

		//while (criterion < numCriterionMetNeeded && iter < maxIter) {
		while (currIter < maxIter && numCriterionMet < BayesianOptimizationOptions.numCriterionNeeded) {
	  		System.out.println("---------");
	  		System.out.println("iter= " + currIter);
	  		System.out.println("---------");

	  		// maximize EI criterion to find the next point to evaluate GP
	  		PointValuePair ret = maxEI.nextPoint(random, gp, fmin);
	  		double [] xNew = ret.getPoint();
	  		double yNew = f.value(xNew);

	  		if (yNew < fmin) {
	  			fmin = yNew;
	  			fopt = xNew;
	  			
		  		System.out.print("New min=" + fmin + " attained at (" );
		  		for (int i = 0; i < fopt.length; i++) {
		  			System.out.print(fopt[i]);
		  			if (i < (fopt.length - 1))
		  				System.out.print(", ");
		  		}
		  		System.out.println(")");
	  		}
	  		currEI = ret.getValue();

	  		// re-fit GP
	  		// merge the data -- the below two lines are linear operations, make it to a constant operation later
	  		X = mergeDesignPoints(X, xNew);
	  		y = mergeResponse(y, yNew);
	  		
	  		// at certain point, need to restrict the parameters within bound to ensure convergence of EI
	  		// for this, we follow the suggestion in REMBO paper that if the algorithm starts exploiting too much, then
	  		// we decrease the upper bound U on the parameters
	  		
	  		double [] muVar = gp.predict(xNew);
	  		double error = Math.abs(muVar[0] - yNew)/Math.sqrt(muVar[1]);
	  		if (error > BayesianOptimizationOptions.errorDev) {
	  			System.out.println("Bad prediction -- re-fit");
	  			System.out.println("predicted=" + muVar[0] + " true=" + yNew + ", sd=" + Math.sqrt(muVar[1]));
	  			
	  			double maxParam = Double.NEGATIVE_INFINITY;
		  		double [] params = gp.getParameters();
	  			for (int i = 0; i < params.length; i++) {
	  				if (params[i] > maxParam) {
	  					maxParam = params[i];
	  				}
	  			}
	  			
  				GPOptions.searchUpperBound = Math.max(0.9*maxParam, GPOptions.searchLowerBound);
	  			
	  			//re-fit GP
  				bounds = new SearchInterval(GPOptions.searchLowerBound, GPOptions.searchUpperBound);
		  		gp = GaussianProcess.fitParameters(random, kernel, X, y, bounds);
		  		System.out.println("GP parameters: ");
		  		for (int i = 0; i < params.length; i++) {
		  			System.out.print(params[i] + " ");
		  		}
		  		System.out.println();
		  		
				System.out.println("-------");
				System.out.println("Data:");
				for (int i = 0; i < X.length; i++) {
					String dat = "";
					for (int j = 0; j < X[i].length; j++) {
						dat += X[i][j] + " ";
					}
					dat += y[i];
					System.out.println(dat);
				}
				System.out.println("-------");
	  		} else {
	  			gp.updateData(X, y);
	  		}

	  		/*
	  		double sdx = Math.sqrt(gp.predict(xNew)[1]);
	  		if (sdx < BayesianOptimizationOptions.tsigma) {
	  			System.out.println("Exploiting...");
	  			gpRefitCounter++;
	  		} else {
	  			System.out.println("Exploring...");
	  			gpRefitCounter = 0;
	  		}
	  		
	  		if (currIter % 20 == 0 || gpRefitCounter == 5) {
	  			double maxParam = Double.NEGATIVE_INFINITY;
		  		double [] params = gp.getParameters();
	  			for (int i = 0; i < params.length; i++) {
	  				if (params[i] > maxParam) {
	  					maxParam = params[i];
	  				}
	  			}
	  			
	  			if (gpRefitCounter == 5) {
	  				GPOptions.searchUpperBound = Math.max(0.9*maxParam, GPOptions.searchLowerBound);
	  				gpRefitCounter = 0;
	  			}
	  			
	  			//re-fit GP
		  		gp = GaussianProcess.fitParameters(random, kernel, X, y, bounds);
		  		System.out.println("GP parameters: ");
		  		for (int i = 0; i < params.length; i++) {
		  			System.out.print(params[i] + " ");
		  		}
		  		System.out.println();
	  		} else {
	  			gp.updateData(X, y);
	  		}
	  		*/
	  		
	  		if (currEI < BayesianOptimizationOptions.eiTolerance) {
	  			numCriterionMet++;
	  		} else {
	  			numCriterionMet = 0;
	  		}

	  		System.out.println("EI=" + currEI + ", loglik=" + gp.getLogLik());
  			computeSobolIndices(random, gpPredict);
	  		System.out.println("---------");
	  		
	  		currIter++;
		}

		System.out.println("------");
		System.out.println("min=" + fmin);
		for (int i = 0; i < fopt.length; i++) {
			System.out.print(fopt[i] + " ");
		}
		System.out.println();
	}
	
	private void computeSobolIndices(Random random, Function gpPredict) {
		for (int u = 0; u < gp.getDim(); u++) {
			SummaryStatistics lowerSobolSummary = new SummaryStatistics();
			SummaryStatistics upperSobolSummary = new SummaryStatistics();

			for (int i = 0; i < BayesianOptimizationOptions.numSobolReplicates; i++) {
				double lowerSobol = Sobol.lower(random, gpPredict, BayesianOptimizationOptions.numSobolSamples, new int[]{u});
				double upperSobol = Sobol.upper(random, gpPredict, BayesianOptimizationOptions.numSobolSamples, new int[]{u});
				lowerSobolSummary.addValue(lowerSobol);
				upperSobolSummary.addValue(upperSobol);
			}
			System.out.println("u=" + u + ", lsobol=" + lowerSobolSummary.getMean() + ", sd=" + lowerSobolSummary.getStandardDeviation()); 
			System.out.println("u=" + u + ", usobol=" + upperSobolSummary.getMean() + ", sd=" + upperSobolSummary.getStandardDeviation());
		}

	}
	
	private double [][] mergeDesignPoints(double [][] X, double [] xNew)
	{
		int n = X.length;
		int d = X[0].length;
		double [][] temp = new double[n + 1][d];
		
		for (int i = 0; i < n; i++)
		{
			temp[i] = X[i];
		}
		
		temp[n] = xNew;
		return temp;
	}
	
	private double [] mergeResponse(double [] y, double yNew)
	{
		int n = y.length;
		double [] temp = new double[n + 1];
		System.arraycopy(y, 0, temp, 0, y.length);
		temp[n] = yNew;
		return temp;
	}

}
