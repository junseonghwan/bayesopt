package gp.kernels;

import org.ejml.simple.SimpleMatrix;

public class SquaredExponential implements Kernel 
{
	private double [] theta;
	
	public SquaredExponential(double [] theta)
	{
		this.theta = theta;
	}

//	@Override
//  public double value( double[] x, double [] y ) {
//		if( x.length != y.length )
//		{
//			throw new RuntimeException();
//		}
//		double d = x.length;
//		double ss = 0;
//		for( int i = 0; i < d; i++ )
//		{
//			ss += ( x[i] - y[i] ) * ( x[i] - y[i] ) * theta[i];
//		}
//		
//		return Math.exp( -ss / 2 );
//  }
	
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
	public void updateParameters(double [] theta)
	{
		this.theta = theta;
	}

	@Override
  public int getDim() {
	  return theta.length;
  }

}
