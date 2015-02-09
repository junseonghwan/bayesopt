package gp;

import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.CholeskyDecomposition;
import org.ejml.factory.DecompositionFactory;
import org.ejml.simple.SimpleMatrix;

import functions.Branin;
import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;

public class GaussianProcess 
{
	private Kernel kernel;
	private SimpleMatrix covMatrix;
	private double [][] X;
	private double [] y;
	private double mu;
	private double sigmaSquared;
	
	private int d;

	public GaussianProcess(Kernel kernel, int d, double mu, double sigmaSquared) {
		this.kernel = kernel;
		this.d = d;
		this.mu = mu;
		this.sigmaSquared = sigmaSquared;
	}
	
	private double [] initialGuess(Random random)
	{
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
		
		SimplexOptimizer simplexOptimizer = new SimplexOptimizer(1e-6, 1e-6);
		
		PointValuePair solution = simplexOptimizer.optimize(new ObjectiveFunction(logLik), new MaxEval(10000), GoalType.MINIMIZE, 
				new SearchInterval(0.0, 10.0), new InitialGuess(initialGuess(random)), new MultiDirectionalSimplex(d));
		
		System.out.println("logLikelihood:" + solution.getValue());
		return solution;
	}

	/**
	 * Compute the log-likelihood of the current state of the Gaussian process
	 * @return
	 */
	public double logLik( double[] thetas ) // thetas should be inputs for logLik, and maybe we should make it as inputs for kernel.getCovarianceMatrix
	{
		kernel.updateParameters(thetas);

		int n = X.length;
		SimpleMatrix xMat = new SimpleMatrix( X );
		SimpleMatrix yMat = new SimpleMatrix( n, 1, false, y );
		covMatrix = kernel.getCovarianceMatrix( xMat );
		
		double[] onesArray = new double[n];
		for( int i = 0; i < n; i++ )
		{
			onesArray[i] = 1;
		}
		SimpleMatrix onesMat = new SimpleMatrix( n, 1, false, onesArray );
		
		double muHat = ( onesMat.transpose().mult( covMatrix.invert() ).mult( yMat ).get( 0 ) / onesMat.transpose().mult( covMatrix.invert() ).mult( onesMat ).get( 0 ) );
		
		double[] muHatArray = new double[n];
		for( int i = 0; i < n; i++ )
		{
			muHatArray[i] = muHat;
		}
		
		SimpleMatrix muHatMat = new SimpleMatrix( n, 1, false, muHatArray );
		
		double sigmaSquared = yMat.minus( muHatMat ).transpose().mult( covMatrix.invert() ).mult( yMat.minus( muHatMat ) ).get( 0 ) / ( n + 0.0 );
		
		double loglik = -(n + 0.0) * Math.log( 2 * Math.PI * sigmaSquared ) / 2 - Math.log( covMatrix.determinant() ) / 2 - (n + 0.0) / 2;
		
		return loglik;
	}

	public void generate(Random random, int n, int d)
	{
		// 1. generate X
		// 2. using X, compute R
		// 3. compute y(X)
		
		this.X = new double[n][d];
		this.y = new double[n];
		
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < d; j++)
			{
				this.X[i][j] = random.nextDouble();
			}
		}
		
		SimpleMatrix xMat = new SimpleMatrix(X);
		SimpleMatrix R = kernel.getCovarianceMatrix(xMat);
		
		CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(R.numRows(), false);
	    
		if( !chol.decompose(R.getMatrix()))
		   throw new RuntimeException("Cholesky failed!");
		        
		SimpleMatrix L = SimpleMatrix.wrap(chol.getT(null));
		
		SimpleMatrix V = new SimpleMatrix(n, 1);
		for( int i = 0; i < V.numRows(); i++ )
		{
			V.set(i, random.nextGaussian());
		}
		
		SimpleMatrix muVector = new SimpleMatrix(n, 1);
		for( int i = 0; i < muVector.numRows(); i++ )
		{
			muVector.set(i, this.mu);
		}
		
		this.y = L.mult(V).scale(this.sigmaSquared).plus(muVector).getMatrix().getData();
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
