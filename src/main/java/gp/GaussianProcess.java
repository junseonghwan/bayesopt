package gp;

import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.CholeskyDecomposition;
import org.ejml.factory.DecompositionFactory;
import org.ejml.simple.SimpleMatrix;

import gp.kernels.Kernel;

public class GaussianProcess 
{
	private Kernel kernel;
	private SimpleMatrix covMatrix;
	private double [][] X;
	private double [] y;
	private double muHat;
	private double varHat;
	
	public GaussianProcess(Kernel kernel) {
		this.kernel = kernel;
	}
	
	public double getMuHat() { return muHat; }
	public double getVarianceHat() { return varHat; }
	
	private double [] initialGuess(Random random)
	{
		int d = kernel.getDim();
		double [] guess = new double[d];
		for (int i = 0; i < d; i++)
		{
			guess[i] = random.nextDouble();
		}
		return guess;
	}
	
	/**
	 * Batch fitting of parameters of GP
	 * @param X - input matrix Nxd, where N is the number of inputs and d is the dimension
	 * @param y - output or response vector Nx1 
	 */
	public PointValuePair fitParameters(Random random, double [][] X, double [] y) { // why don't we just access X and y directly?
		
		this.X = X;
		this.y = y;
		
		MultivariateFunction logLik = new MultivariateFunction() {
			
			@Override
			public double value(double[] point) {
				return logLik(point);
			}
		};
		
		SimplexOptimizer simplexOptimizer = new SimplexOptimizer(1e-3, 1e-3);
		
		
		PointValuePair solution = simplexOptimizer.optimize(new ObjectiveFunction(logLik), new MaxEval(1000), GoalType.MAXIMIZE, new InitialGuess(initialGuess(random)), new NelderMeadSimplex(kernel.getDim()));
		
		System.out.println("logLikelihood:" + solution.getValue());
		return solution;
	}

	/**
	 * Compute the log-likelihood of the current state of the Gaussian process
	 * @return
	 */
	public double logLik( double[] thetas ) // thetas should be inputs for logLik, and maybe we should make it as inputs for kernel.getCovarianceMatrix
	{
		for (int i = 0; i < thetas.length; i++)
		{
			if (thetas[i] < 0)
				return Double.NEGATIVE_INFINITY;
		}
		kernel.updateParameters(thetas);

		int n = X.length;
		SimpleMatrix xMat = new SimpleMatrix( X );
		SimpleMatrix yMat = new SimpleMatrix( n, 1, false, y );
		covMatrix = kernel.getCovarianceMatrix( xMat );
		//System.out.println("in generate(): " + covMatrix.toString());
		
		double[] onesArray = new double[n];
		for( int i = 0; i < n; i++ )
		{
			onesArray[i] = 1;
		}
		
		SimpleMatrix Rinv = covMatrix.invert();
		
		SimpleMatrix onesMat = new SimpleMatrix( n, 1, false, onesArray );
		
		//this.muHat = ( onesMat.transpose().mult( Rinv ).dot( yMat ) / onesMat.transpose().mult( Rinv ).dot( onesMat ) );
		this.muHat = ( Rinv.mult(onesMat).dot( yMat ) / Rinv.mult(onesMat).dot( onesMat ) );
		
		double[] muHatArray = new double[n];
		for( int i = 0; i < n; i++ )
		{
			muHatArray[i] = this.muHat;
		}
		
		SimpleMatrix muHatMat = new SimpleMatrix( n, 1, false, muHatArray );
		
		this.varHat = yMat.minus( muHatMat ).transpose().mult( Rinv ).dot( yMat.minus( muHatMat ) ) / n;
		
		double loglik = -n * Math.log( 2 * Math.PI * this.varHat ) / 2.0 - Math.log( covMatrix.determinant() ) / 2.0 - n/2.0;
		for (int i = 0; i < kernel.getDim(); i++)
		{
			System.out.print(thetas[i] + " ");
		}
		System.out.println("loglik=" + loglik);
		return loglik;
	}

	/**
	 * Given the design points, X, generate the values
	 * @param random
	 * @param X - design points
	 */
	public void generate(Random random, double [][] X, double mu, double var)
	{
		int n = X.length;
		this.X = X;
		this.y = new double[n];

		SimpleMatrix xMat = new SimpleMatrix(X);
		SimpleMatrix R = kernel.getCovarianceMatrix(xMat);
	
		CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(R.numRows(), false);

		if( !chol.decompose(R.getMatrix()))
		{
		  throw new RuntimeException("Cholesky decomposition failed! Most likely an issue with the design points");
		}

		SimpleMatrix L = SimpleMatrix.wrap(chol.getT(null)).transpose();

		SimpleMatrix V = new SimpleMatrix(n, 1);
		for( int i = 0; i < V.numRows(); i++ )
		{
			V.set(i, random.nextGaussian());
		}
		
		SimpleMatrix muVector = new SimpleMatrix(n, 1);
		for( int i = 0; i < muVector.numRows(); i++ )
		{
			muVector.set(i, mu);
		}
		
		this.y = L.mult(V).scale(var).plus(muVector).getMatrix().getData();
	}
	
	public double [][] getX()
	{
		return X;
	}
	
	public double [] getY()
	{
		return y;
	}
}
