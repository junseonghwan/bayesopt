package design;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import bayonet.distributions.Random2RandomGenerator;

public class LatinHypercubeSampling 
{

	public LatinHypercubeSampling() {
		
	}
	
	/**
	 * Generate design points according to Latin Hypercube sampling algorithm on [0, 1]^d
	 * @param n - number of points to generate
	 * @param d - input dimension
	 * @return nxd double array of values in [0, 1]
	 */
	public static double [][] sample(Random random, int n, int d) {
		List<Integer> c = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
		{
			c.add(i);
		}

		RandomGenerator rand = new Random2RandomGenerator(random);
		RandomDataGenerator randomData = new RandomDataGenerator(rand);

		Integer [][] Z = new Integer[n][d];
		for (int i = 0; i < d; i++)
		{
			Object [] obj = randomData.nextSample(c, n);
			for (int j = 0; j < n; j++)
			{
				Z[j][i]= (Integer)obj[j];
				//System.out.print(Z[j][i] + " ");
			}
			//System.out.println();
		}
		
		double [][] X = new double[n][d];
		
		for (int i = 0; i < n; i++)
		{
			for (int j = 0; j < d; j++)
			{
				int a = Z[i][j];
				double u = random.nextDouble();
				X[i][j] = (u + a)/n;
				System.out.print(X[i][j] + " ");
			}
			System.out.println();
		}

		return X;
	}
	
	
}
