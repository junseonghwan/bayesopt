package gp.kernels;

import org.ejml.simple.SimpleMatrix;

public class Matern implements Kernel {

	@Override
	public double value(double[] x, double [] y) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
  public SimpleMatrix getCovarianceMatrix(SimpleMatrix X) {
	  // TODO Auto-generated method stub
		int p = X.numCols();
		SimpleMatrix r = new SimpleMatrix( p, p );
		for( int i = 0; i < p; i++ )
		{
			for( int j = 0; j <= i; j++ )
			{
				r.set( i, j, value( X.extractVector(false, i).getMatrix().getData(), 
									X.extractVector(false, j).getMatrix().getData() ) );
				r.set( j, i, r.get(i, j) );
			}
		}
		
		return r;
  }

	@Override
	public void updateParameters(double[] params) {
		// TODO Auto-generated method stub
		
	}

}
