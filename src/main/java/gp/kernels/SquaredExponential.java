package gp.kernels;

import javax.management.RuntimeErrorException;

import org.ejml.simple.SimpleMatrix;

public class SquaredExponential implements Kernel 
{
	private double [] theta;
	
	public SquaredExponential(double [] theta)
	{
		this.theta = theta;
	}

	@Override
  public double value( double[] x, double [] y ) {
		if( x.length != y.length )
		{
			throw new RuntimeException();
		}
		double d = x.length;
		double ss = 0;
		for( int i = 0; i < d; i++ )
		{
			ss += ( x[i] - y[i] ) * ( x[i] - y[i] ) / theta[i];
		}
		
		return Math.exp( -ss / 2 );
  }

	@Override
  public SimpleMatrix getCovarianceMatrix(SimpleMatrix X) {
	  // TODO Auto-generated method stub
		int n = X.numRows();
		SimpleMatrix r = new SimpleMatrix( n, n );
		for( int i = 0; i < n; i++ )
		{
			for( int j = 0; j <= i; j++ )
			{
				r.set( i, j, value( X.extractVector(true, i).getMatrix().getData(), 
									X.extractVector(true, j).getMatrix().getData() ) );
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

}
