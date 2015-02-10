package gp.kernels;

import org.ejml.simple.SimpleMatrix;

public interface Kernel
{
	public int getDim();
	public double value(SimpleMatrix x, SimpleMatrix y);
	public SimpleMatrix getCovarianceMatrix(SimpleMatrix X);
	
	public double [] getParameters();
	public void updateParameters(double [] params);

}
