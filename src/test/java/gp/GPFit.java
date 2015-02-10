package gp;

import java.util.Random;

import org.apache.commons.math3.optim.univariate.SearchInterval;

import design.LatinHypercubeSampling;
import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;
import briefj.run.Mains;

public class GPFit implements Runnable
{
	
	public void run() {
		double [] theta = new double[]{0.5, Math.PI, 0.1};
		Kernel kernel = new SquaredExponential(theta);
		
		Random random = new Random(1230);
		// adding more data doesn't necessarily equate to more accurate parameter estimates, is this OK?
		int n = 50;
		int d = theta.length;
		double [][] X = LatinHypercubeSampling.sample(random, n, d);
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < d; j++)
			{
				System.out.print(X[i][j] + " ");
			}
			System.out.println();
		}
		
		System.out.println("---------------");

		double mu = 0.0, var = 1.2;
		double [] y = GaussianProcess.generate(random, kernel, X, mu, var);

		System.out.println("y=");
		for (int i = 0; i < n; i++)
		{
			System.out.println(y[i]);
		}

		SearchInterval bounds = new SearchInterval(1e-3, 5.0);
		GaussianProcess gp = GaussianProcess.fitParameters(random, kernel, X, y, bounds);
		double [] parameterEstimates = gp.getParameters();
		for (int i = 0; i < theta.length; i++)
		{
			System.out.println(parameterEstimates[i] + " vs " + theta[i]); // not bad!!
		}
		
		System.out.println(gp.getMuHat() + " vs " + 0); // pretty bad... is there a problem?
		System.out.println(gp.getVarianceHat() + " vs " + 1); // not bad
	}
	
	public static void main(String [] args) {
		Mains.instrumentedRun(args, new GPFit());
	}

}
