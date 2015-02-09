package gp.kernels;

import org.ejml.simple.SimpleMatrix;

public class Matern implements Kernel {


	@Override
  public SimpleMatrix getCovarianceMatrix(SimpleMatrix X) {
	  // TODO Auto-generated method stub
		return null;
  }

	@Override
	public void updateParameters(double[] params) {
		// TODO Auto-generated method stub
		
	}

	@Override
  public double value(SimpleMatrix x, SimpleMatrix y) {
	  // TODO Auto-generated method stub
	  return 0;
  }

	@Override
  public int getDim() {
	  // TODO Auto-generated method stub
	  return 0;
  }

}
