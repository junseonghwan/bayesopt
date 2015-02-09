package gp;

import java.util.Random;

import org.apache.commons.math3.optim.PointValuePair;

import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;
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
		
		double [] thetas = new double[]{1.0, 0.5};
		Kernel kernel = new SquaredExponential(thetas);
		GaussianProcess gp = new GaussianProcess(kernel, 2, 0, 1.0);
		Random random = new Random(1230);
		int n = 10;
		gp.generate(random, n, 2);
		for (int i = 0; i < n; i++)
		{
			System.out.println(gp.getX()[i][0] + ", " + gp.getX()[i][1] + ", " + gp.getY()[i]);
		}

		System.out.println(gp.logLik(thetas));
	}
	
	public static void main(String [] args) {
		Mains.instrumentedRun(args, new GPFit());
	}

}
