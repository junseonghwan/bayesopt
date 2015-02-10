package gp;

import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;

import java.util.Random;

import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.junit.Test;

public class GPFitTest {

	@Test
	public void test() {
		GPOptions.verbose = false;
		Random random = new Random(10);
		
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

		// do it multiple times to get a good value
		int numIter = 20;
		for (int iter = 0; iter < numIter; iter++) {
			double init = random.nextDouble() + 1;
  		Kernel k2 = new SquaredExponential(new double[]{init});
  		SearchInterval bounds = new SearchInterval(1e-3, 5.0);
  		GaussianProcess gp = GaussianProcess.fitParameters(random, k2, X, y, bounds);
  		System.out.println("--------");
  		System.out.println("init=" + init);
  		System.out.println(gp.getLogLik() + ", " + gp.getMuHat() + ", " + gp.getVarianceHat()); // the muHat is off by 1, and the var is also a bit off
  		double [] est = gp.getParameters();
  		for (int i = 0; i < est.length; i++) {
  			System.out.println(est[i] + " vs " + theta[i]); // the estimate is not too bad
  		}
  		System.out.println("--------");
		}
		
		
		// conclusion: to allow for more data, GP requires nugget term to be included, which might cause drop-off in the estimation?

	}
}
