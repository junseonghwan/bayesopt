package gp;

import org.ejml.simple.SimpleMatrix;

import gp.kernels.Kernel;

public class GaussianProcess 
{
	private Kernel kernel;
	private SimpleMatrix covMatrix;
	private double [][] X;
	private double [] y;

	public GaussianProcess(Kernel kernel) {
		this.kernel = kernel;
	}
	
	/**
	 * Batch fitting of parameters of GP
	 * @param X - input matrix Nxd, where N is the number of inputs and d is the dimension
	 * @param y - output or response vector Nx1 
	 */
	public void fitParameters(double [][] X, double [] y) {
		// use Apache optimization library
	}

	/**
	 * Compute the log-likelihood of the current state of the Gaussian process
	 * @return
	 */
  public double logLik() {
		return 0.0;
	}
	
	
}
