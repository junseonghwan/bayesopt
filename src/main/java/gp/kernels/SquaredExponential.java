package gp.kernels;

import gp.GPOptions;

import org.ejml.simple.SimpleMatrix;

public class SquaredExponential implements Kernel 
{
	private double [] theta;
	
	public SquaredExponential(int d) {
		// randomly initialize theta
		theta = new double[d];
		for (int i = 0; i < d; i++) {
			theta[i] = GPOptions.random.nextDouble()*GPOptions.searchUpperBound;
		}
	}
	
	public SquaredExponential(double [] theta)
	{
		this.theta = theta;
	}

	@Override
	public double value(SimpleMatrix x, SimpleMatrix y)
	{
		if( x.numRows() != y.numRows() || x.numCols() != 1 || y.numCols() != 1)
		{
			throw new RuntimeException();
		}
		SimpleMatrix param = new SimpleMatrix(theta.length, 1, false, theta);
		SimpleMatrix diff = x.minus(y);
		double ss = diff.elementMult(diff).dot(param);
		return Math.exp(-ss);
	}

	@Override
  public SimpleMatrix getCovarianceMatrix(SimpleMatrix X) {
		int n = X.numRows();
		SimpleMatrix r = new SimpleMatrix( n, n );
		for( int i = 0; i < n; i++ )
		{
			for( int j = i; j < n; j++ )
			{
				r.set( i, j, value(X.extractVector(true, i).transpose(), X.extractVector(true, j).transpose()));
				r.set( j, i, r.get(i, j) );
			}
		}

		return r;
  }
	
	@Override
	public double [] getParameters() {
		return this.theta;
	}

	@Override
	public void updateParameters(double [] theta)
	{
		this.theta = theta;
	}

	@Override
  public int getDim() {
	  return theta.length;
  }

}
