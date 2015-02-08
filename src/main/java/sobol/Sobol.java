package sobol;
import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import functions.Function;

public class Sobol 
{
	public static void drawUniform(Random random, double [] z)
	{
		int n = z.length;
		for (int j = 0; j < n; j++)
		{
			z[j] = random.nextDouble();
		}
	}
	
	public static double lower(Random random, Function func, int n, int [] u)
	{
		int d = func.getDim();
		
		double [][] xx = new double[d][n];
		double [][] zz = new double[d][n];
		
		for (int i = 0; i < d; i++)
		{
			for (int j = 0; j < n; j++)
			{
				xx[i][j] = random.nextDouble();
				zz[i][j] = random.nextDouble();
			}
		}
		
		for (int i : u)
		{
			System.arraycopy(xx[i], 0, zz[i], 0, xx[i].length);
		}
		
		SimpleMatrix xxMat = new SimpleMatrix(xx);
		SimpleMatrix zzMat = new SimpleMatrix(zz);
		xxMat = xxMat.transpose();
		zzMat = zzMat.transpose();

		double [] fxx = func.eval(xxMat);
		double [] fzz = func.eval(zzMat);

		double mu_hat = 0;
		double cross_moment = 0;
		for (int i = 0; i < n; i++)
		{
			mu_hat += (fxx[i] + fzz[i]);
			cross_moment += (fxx[i] * fzz[i]);
		}
		
		mu_hat = mu_hat/(2*n);
		cross_moment /= n;
		
		return cross_moment - mu_hat * mu_hat;
	}
	
	public static double upper(Random random, Function func, int n, int [] u)
	{
		int d = func.getDim();
		
		double [][] xx = new double[d][n];
		double [][] zz = new double[d][n];
		
		for (int i = 0; i < d; i++)
		{
			for (int j = 0; j < n; j++)
			{
				xx[i][j] = random.nextDouble();
				zz[i][j] = xx[i][j];
			}
		}
		
		// draw different uniform numbers for the rows r in u
		for(int i : u)
		{
			drawUniform(random, zz[i]);
		}
		
		SimpleMatrix xxMat = new SimpleMatrix(xx);
		SimpleMatrix zzMat = new SimpleMatrix(zz);
		xxMat = xxMat.transpose();
		zzMat = zzMat.transpose();

		double[] fxx = func.eval(xxMat);
		double[] fzz = func.eval(zzMat);
		
		double usobol = 0;
		for( int i = 0; i < n; i++ )
		{
			usobol += (fxx[i] - fzz[i]) * (fxx[i] - fzz[i]);
		}

		usobol/=  (2 * n);
		return usobol;
	}

}
