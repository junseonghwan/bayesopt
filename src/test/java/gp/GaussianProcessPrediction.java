package gp;

import java.util.Random;

import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;

import org.junit.Assert;
import org.junit.Test;

import bayonet.math.NumericalUtils;

public class GaussianProcessPrediction {
	
	@Test
	public void test() {
		
		Random random = new Random(10);
		GPOptions.verbose = true;

		double [] theta = new double[]{1.0}; 
		Kernel kernel = new SquaredExponential(theta);
		int n = 10;
		double [][] X = new double[n][1];
		for (int i = 0; i < n; i++)
		{
			X[i][0] = (double)i/n;
		}
		double mu = 1.0, var = 2.0;
		double [] y = GaussianProcess.generate(random, kernel, X, mu, var);
		
		System.out.println("--------");
		System.out.println("y=");
		for (int i = 0; i < n; i++)
		{
			System.out.println(y[i]);
		}
		System.out.println("--------");

		/*
		 * Under the above setting, with the seed = 10, R produced the following outputs, which match the output from generate() function
		 * 
		 * 2.236983 2.041721 1.874448 1.739178 1.637358 1.568602 1.531239 1.522593 1.539042 1.575971
		 * 
		 * TODO: write a test to validate generate()
		 */
		
		GaussianProcess gp = new GaussianProcess(kernel, X, y, mu, var);

		// now predict
		double [] input = new double[1];
		double ss = 0.0;
		for (int index = 0; index < n; index++)
		{
  		input[0] = X[index][0]; // should be same as gp.y[index] if the true mu and true var are used
  		double [] muVar = gp.predict(input);
  		ss += Math.pow(muVar[0] - y[index], 2.0);
  		System.out.println(y[index] + " vs " + muVar[0] + ", " + muVar[1]);
  		if (!NumericalUtils.isClose(muVar[0], y[index], 1e-1)) // just check that they are close enough
  		{
  			Assert.fail();
  		}
		}
		
		System.out.println(Math.sqrt(ss/n));
		
		// conclusion: the error for prediction is too large, is this because of matrix inversion, numerical instability?
	}

}
