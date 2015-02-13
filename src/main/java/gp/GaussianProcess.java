package gp;

import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.CholeskyDecomposition;
import org.ejml.factory.DecompositionFactory;
import org.ejml.simple.SimpleMatrix;

import util.EJMLUtil;
import gp.kernels.Kernel;

public class GaussianProcess 
{
	private Kernel kernel;
	private SimpleMatrix covMatrix;
	private SimpleMatrix covInverse;
	private double [][] X;
	private double [] y;
	private double muHat;
	private double varHat;
	private double loglik;
	
	public GaussianProcess(Kernel kernel, double [][] X, double [] y, double mu, double var) {
		this.kernel = kernel;
		this.X = X;
		this.y = y;
		this.muHat = mu;
		this.varHat = var;
		
		// compute the covMatrix
		SimpleMatrix xMat = new SimpleMatrix(X);
		this.covMatrix = kernel.getCovarianceMatrix(xMat);
		this.covInverse = EJMLUtil.invertSymmetricMatrix(this.covMatrix);
	}
	
	/**
	 * internal use for fitting of GP -- does not allow mu and var to be specified.
	 * @param kernel
	 * @param X
	 * @param y
	 */
	private GaussianProcess(Kernel kernel, double [][] X, double [] y) {
		this(kernel, X, y, 0.0, 1.0);
	}
	
	/**
	 * Batch fitting of parameters of GP and return the fitted GP
	 * @param X - input matrix Nxd, where N is the number of inputs and d is the dimension
	 * @param y - output or response vector Nx1 
	 */
	public static GaussianProcess fitParameters(Random random, Kernel kernel, double [][] X, double [] y, SearchInterval bounds) {
		
		GaussianProcess gp = new GaussianProcess(kernel, X, y);
		gp.X = X;
		gp.y = y;
		
		MultivariateFunction logLik = new MultivariateFunction() {
			@Override
			public double value(double[] point) {
				gp.kernel.updateParameters(point);
				return gp.logLik();
			}
		};
		
		if (GPOptions.verbose) {
	  		System.out.println("Begin GP fitting");
	  		System.out.println("-----------------");
		}

		double maxLogLik = Double.NEGATIVE_INFINITY;
		PointValuePair optimal = null;
		double [] initialGuess;
		for (int i = 0; i < GPOptions.numRestarts; i++) {
			initialGuess = GPOptions.randomGuess(gp.getDim());
			SimplexOptimizer simplexOptimizer = new SimplexOptimizer(1e-5, 1e-6);
			PointValuePair solution = simplexOptimizer.optimize(new ObjectiveFunction(logLik), new MaxEval(1000), GoalType.MAXIMIZE, bounds,
					new InitialGuess(initialGuess), new NelderMeadSimplex(kernel.getDim()));
					//new InitialGuess(kernel.getParameters()), new NelderMeadSimplex(kernel.getDim()));
			if (solution.getValue() > maxLogLik) {
				optimal = solution;
				maxLogLik = optimal.getValue();
			}
		}
		
		gp.loglik = optimal.getValue().doubleValue();
		gp.kernel.updateParameters(optimal.getPoint());
		
		if (GPOptions.verbose) {
		  	System.out.println("-----------------");
	  		System.out.println("end GP fitting");
	  		System.out.println("loglik=" + gp.loglik);
		}
		return gp;
	}

	/**
	 * Compute the log-likelihood of the current state of the Gaussian process -- internal use for GP fitting 
	 * @return
	 */
	private double logLik()
	{
		double [] params = this.kernel.getParameters();
		for (int i = 0; i < params.length; i++)
		{
			if (params[i] <= GPOptions.searchLowerBound || params[i] >= GPOptions.searchUpperBound)
				return Double.NEGATIVE_INFINITY;
		}

		int n = X.length;
		SimpleMatrix xMat = new SimpleMatrix(X);
		SimpleMatrix yVector = new SimpleMatrix(n, 1, false, y);
		covMatrix = kernel.getCovarianceMatrix(xMat);

		/*
		 * The below code contains matrix operations, which are bottleneck of GP fitting -- i.e., involves matrix inversion
		 */

		// we might need to add the nugget term to avoid the singularity problem!
		this.covInverse = EJMLUtil.invertSymmetricMatrix(this.covMatrix);
 		this.muHat = covInverse.mult(yVector).elementSum()/covInverse.elementSum();

		SimpleMatrix muHatVector = EJMLUtil.rep(n, this.muHat);
		SimpleMatrix diff = yVector.minus(muHatVector);

		this.varHat = diff.transpose().mult(covInverse).dot(diff) / n;
		
		double loglik = -n * Math.log( 2 * Math.PI * this.varHat ) / 2.0 - Math.log( covMatrix.determinant() ) / 2.0 - n/2.0;
		if (Double.isNaN(loglik)) {
			return Double.NEGATIVE_INFINITY;
		}

		if (GPOptions.verbose) {
	  		for (int i = 0; i < kernel.getDim(); i++)
	  		{
	  			System.out.print(params[i] + " ");
	  		}
	  		System.out.println("; loglik=" + loglik);
		}
		return loglik;
	}

	/**
	 * Generate the realization from a GP at the design points, X
	 * @param random
	 * @param kernel - the kernel for GP
	 * @param X - design points
	 * @param mu - the mean
	 * @param var - the variance
	 */
	public static double [] generate(Random random, Kernel kernel, double [][] X, double mu, double var)
	{
		int n = X.length;

		SimpleMatrix xMat = new SimpleMatrix(X);
		SimpleMatrix covMatrix = kernel.getCovarianceMatrix(xMat);

		CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(covMatrix.numRows(), false);

		if( !chol.decompose(covMatrix.getMatrix()))
		{
		  throw new RuntimeException("Cholesky decomposition failed! Most likely an issue with the design points.");
		}

		SimpleMatrix L = SimpleMatrix.wrap(chol.getT(null)).transpose();

		SimpleMatrix V = new SimpleMatrix(n, 1);
		// output L and V
		if (GPOptions.verbose) {
  		System.out.println("---------");
  		System.out.println("V=");
  		for( int i = 0; i < V.numRows(); i++ )
  		{
  			V.set(i, random.nextGaussian());
  			System.out.println(V.get(i));
  		}
  		
  		System.out.println("L=");
  		System.out.println(L.toString());
  		System.out.println("---------");
		} else {
  		for( int i = 0; i < V.numRows(); i++ )
  		{
  			V.set(i, random.nextGaussian());
  		}
		}

		SimpleMatrix muVector = EJMLUtil.rep(n, mu);

		double [] y = L.mult(V).scale(Math.sqrt(var)).plus(muVector).getMatrix().getData();
		return y;
	}
	
	/**
	 * predicts at the given input
	 * @param input - the input at which the prediction is to be made
	 * @return the predicted value and its variance in the first and second components
	 */
	public double [] predict(double [] input) {
		
		if (input.length != this.kernel.getDim()) {
			throw new RuntimeException("The dimension of the input does not match the dimension of the GP");
		}
		
		int n = y.length;
		SimpleMatrix yVector = new SimpleMatrix(n, 1, false, y);
		SimpleMatrix corrVector = new SimpleMatrix(n, 1, false, computeCorrelation(input));
		SimpleMatrix muVector = EJMLUtil.rep(n, this.muHat);
		
		double [] muVar = new double[2];
		muVar[0] = this.muHat + corrVector.transpose().mult(covInverse).dot(yVector.minus(muVector));
		SimpleMatrix temp = covInverse.mult(corrVector);
		muVar[1] = Math.max(this.varHat*(1 - corrVector.transpose().dot(temp) + Math.pow(1 - temp.elementSum(), 2.0)/covInverse.elementSum()), 0.0);
		return muVar;
	}
	
	private double [] computeCorrelation(double [] input) {
		double [] r = new double[y.length];
		SimpleMatrix inputVector = new SimpleMatrix(input.length, 1, false, input);
		for (int i = 0; i < X.length; i++)
		{
			SimpleMatrix xVector = new SimpleMatrix(input.length, 1, false, X[i]);
			r[i] = kernel.value(inputVector, xVector);			
		}
		
		return r;
	}
	
	public void updateData(double [][] X, double [] y) {
		this.X = X;
		this.y = y;
		
		int n = X.length;

		SimpleMatrix xMat = new SimpleMatrix(X);
		SimpleMatrix yVector = new SimpleMatrix(n, 1, false, y);

		covMatrix = kernel.getCovarianceMatrix(xMat);

		/*
		 * The below code contains matrix operations, which are bottleneck of GP fitting -- i.e., involves matrix inversion
		 */

		// we might need to add the nugget term to avoid the singularity problem!
		this.covInverse = EJMLUtil.invertSymmetricMatrix(this.covMatrix);
		this.muHat = covInverse.mult(yVector).elementSum()/covInverse.elementSum();

		SimpleMatrix muHatVector = EJMLUtil.rep(n, this.muHat);
		SimpleMatrix diff = yVector.minus(muHatVector);

		this.varHat = diff.transpose().mult(covInverse).dot(diff) / n;

		this.loglik = -n * Math.log( 2 * Math.PI * this.varHat ) / 2.0 - Math.log( covMatrix.determinant() ) / 2.0 - n/2.0;
		//System.out.println("loglik=" + loglik);
	}
	
	public double [][] getX() { return X; }
	public double [] getY() { return y; }
	public double getMuHat() { return muHat; }
	public double getVarianceHat() { return varHat; }	
	public double getLogLik() { return loglik; }
	public double [] getParameters() { return kernel.getParameters(); }
	public int getDim() { return kernel.getDim(); }

}
