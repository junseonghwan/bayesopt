package bo;

import java.util.Random;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.ejml.simple.SimpleMatrix;

import sobol.Sobol;
import design.LatinHypercubeSampling;
import functions.Function;
import gp.GPOptions;
import gp.GaussianProcess;
import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;

public class BayesianOptimization {

	private GaussianProcess gp;
	
	public BayesianOptimization()
	{
	}
	
	/**
	 * 
	 * @param f - the function to find the minimum
	 * @param gp - GP with fixed Kernel parameter to be used for modelling f 
	 * @param n0 - initial design points to generate
	 * @param maxIter - maximum number of iterations or budget
	 */
	public void minimize(Random random, Function f, int n0, int maxIter) {
		
		// The standard approach is as follows:
		// 1. generate initial design points, D and evaluate f(D)
		// 2. batch fit GP on these initial design points
		// 3. maximize EI criterion to find the next point, x,  to evaluate
		// 4. evaluate f(x), add (x, f(x)) to the training data, D' = D + (x, f(x))
		// 5. re-fit GP on D'
		// 6. repeat until budget is reached or the algorithm converged
		
		double [][] X = LatinHypercubeSampling.sample(random, n0, f.getDim());
		SimpleMatrix xMat = new SimpleMatrix(X);
		double [] y = f.value(xMat);
		
		int d = f.getDim();
		double [] initialTheta = new double[d];
		for (int i = 0; i < d; i++) {
			initialTheta[i] = random.nextDouble();
		}
		
		Kernel kernel = new SquaredExponential(initialTheta);
		
		SearchInterval bounds = new SearchInterval(GPOptions.searchLowerBound, GPOptions.searchUpperBound);
		gp = GaussianProcess.fitParameters(random, kernel, X, y, bounds);
		System.out.println(gp.getLogLik());
		
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
		
		double min  = Double.POSITIVE_INFINITY;
		double [] optPoint = null;
		for (int i = 0; i < y.length; i++) {
			if (y[i] < min) {
				min = y[i];
				optPoint = X[i];
			}
		}

		MaxImprovement maxEI = new MaxImprovement(min);

		double EI = Double.POSITIVE_INFINITY;
		double tol = 0;
		int iter = 0;
		int criterion = 0;
		int numCriterionMetNeeded = 2;

		int numSobolSamples = 1000;
		int numRep = 10;
		double [] lsobol = new double[numRep]; 
		double [] usobol = new double[numRep]; 
		double [] lastLowerSobol = new double[numRep];
		double [] lastUpperSobol = new double[numRep];
		//MannWhitneyUTest test = new MannWhitneyUTest();
		TTest ttest = new TTest();

		//while (criterion < numCriterionMetNeeded && iter < maxIter) {
		while (iter < maxIter) {
	  		// maximize EI criterion to find the next point to evaluate GP
	  		PointValuePair ret = maxEI.nextPoint(random, gp, optPoint);
	  		double [] xNew = ret.getPoint();
	  		double yNew = f.value(xNew);

	  		if (yNew < min) {
	  			min = yNew;
	  			optPoint = xNew;
	  		}

	  		// re-fit GP
	  		// merge the data -- the below two lines are linear operations, make it to a constant operation later
	  		X = mergeDesignPoints(X, xNew);
	  		y = mergeResponse(y, yNew);
	  		gp = GaussianProcess.fitParameters(random, kernel, X, y, bounds);
	  		EI = ret.getValue();
	  		iter++;

	  		if (EI < tol) {
	  			criterion++;
	  		} else {
	  			criterion = 0;
	  		}

	  		//double maxDiff = Double.NEGATIVE_INFINITY;
	  		boolean exit = false;
				for (int u = 0; u < gp.getDim(); u++) {
					for (int i = 0; i < numRep; i++) {
						lastLowerSobol[i] = lsobol[i];
						lastUpperSobol[i] = usobol[i];

						lsobol[i] = Sobol.lower(random, gpPredict, numSobolSamples, new int[]{u});
						usobol[i] = Sobol.upper(random, gpPredict, numSobolSamples, new int[]{u});
					}

					//System.out.println("u=" + u + ", lsobol=" + lsobol + ", usobol=" + usobol);

					/*
					double relDiff = checkDiff(lsobol, lastLowerSobol, usobol, lastUpperSobol);
					if (relDiff > maxDiff) {
						maxDiff = relDiff;
					}
					*/
					
					if (iter > 15) {
  					//double plower = test.mannWhitneyU(lastLowerSobol, lsobol);
  					//double pupper = test.mannWhitneyU(lastUpperSobol, usobol);
						double plower = ttest.tTest(lastLowerSobol, lsobol);
  					double pupper = ttest.tTest(lastUpperSobol, usobol);
  					System.out.println("plower=" + plower + ", pupper=" + pupper);
  					if (plower > 0.05 && pupper > 0.05) {
  						exit = true;
  					}
					}
					
				}
				
				if (exit)
					break;
				
				/*
				System.out.println("maxDiff = " + maxDiff);
				if (maxDiff < 0.05) {
					break;
				}
				*/
	
	  		System.out.println("iter= " + iter + ", EI=" + EI + ", loglik=" + gp.getLogLik() + ", currMin=" + min );
		}

		System.out.println("------");
		System.out.println("min=" + min);
		for (int i = 0; i < optPoint.length; i++) {
			System.out.print(optPoint[i] + " ");
		}
		System.out.println();
		System.out.println("------");
	}
	
	private double checkDiff(double lsobol, double lastLowerSobol, double usobol, double lastUpperSobol) {
		double diff1 = Math.abs(lsobol - lastLowerSobol)/lastLowerSobol;
		double diff2 = Math.abs(usobol - lastUpperSobol)/lastUpperSobol;
		return Math.max(diff1, diff2);
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
