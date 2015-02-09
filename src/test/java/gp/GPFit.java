package gp;

import java.util.Random;

import org.apache.commons.math3.optim.PointValuePair;
import org.junit.Assert;

import design.LatinHypercubeSampling;
import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;
import bayonet.math.NumericalUtils;
import briefj.run.Mains;

public class GPFit implements Runnable
{
	
	public void run() {
		/*
		Kernel kernel = new SquaredExponential( new double[] {1.0, 0.5} );
		GaussianProcess gp = new GaussianProcess(kernel, 2, 0, 1.0);
		Random random = new Random(1230);
		gp.generate(random, 50, 2);
		PointValuePair solution = gp.fitParameters(random, gp.getX(), gp.getY());
		double [] x = solution.getPoint();
		System.out.println(x[0] + ", " + x[1]);
		*/
		
		double [] theta = new double[]{2.7, Math.PI, 0.1};
		Kernel kernel = new SquaredExponential(theta);
		GaussianProcess gp = new GaussianProcess(kernel);
		
		Random random = new Random(1230);
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

		gp.generate(random, X, 0, 1.0);
		for (int i = 0; i < n; i++)
		{
			System.out.println(gp.getY()[i]);
		}

		System.out.println("logLik at true theta=" + gp.logLik(theta));
		System.out.println(gp.getMuHat() + ", " + gp.getVarianceHat());

		PointValuePair solution = gp.fitParameters(random, X, gp.getY());
		System.out.println("logLik at estimated theta=" + solution.getValue());
		double [] parameterEstimates = solution.getPoint();
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
