package gp.kernels;

import org.ejml.simple.SimpleMatrix;

public interface Kernel {

	public double value(double [] x, double [] y);
	public SimpleMatrix getCovarianceMatrix(SimpleMatrix X);

}
