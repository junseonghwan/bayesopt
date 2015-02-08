package gp.kernels;

import org.ejml.simple.SimpleMatrix;

public class SquaredExponential implements Kernel 
{

	@Override
  public double value(double[] x, double [] y) {
	  return 0;
  }

	@Override
  public SimpleMatrix getCovarianceMatrix(SimpleMatrix X) {
	  // TODO Auto-generated method stub
	  return null;
  }

}
