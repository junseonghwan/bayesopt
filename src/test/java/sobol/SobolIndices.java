package sobol;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Assert;
import org.junit.Test;

import functions.Branin;
import functions.Function;

public class SobolIndices
{
	@Test
	public void test() {
		Random random = new Random(232);
		Function branin = new Branin();
		int n = 100000;
		int d = branin.getDim();
		int nRuns = 20;
		
		double [][] lsobol = new double[d][nRuns];
		double [][] usobol = new double[d][nRuns];
		SummaryStatistics [] lowerSumm = new SummaryStatistics[2];
		SummaryStatistics [] upperSumm = new SummaryStatistics[2];
		for (int i = 0; i < nRuns; i++)
		{
  		for (int u = 0; u < branin.getDim(); u++)
  		{
  			if (lowerSumm[u] == null)
  			{
  				lowerSumm[u] = new SummaryStatistics();
  			}
  			if (upperSumm[u] == null)
  			{
  				upperSumm[u] = new SummaryStatistics();
  			}
  			
  			lsobol[u][i] = Sobol.lower(random, branin, n, new int[]{u});
  			usobol[u][i] = Sobol.upper(random, branin, n, new int[]{u});

  			lowerSumm[u].addValue(lsobol[u][i]);
  			upperSumm[u].addValue(usobol[u][i]);
  			//System.out.println(lsobol[u][i] + ", " + usobol[u][i]);
  			if (lsobol[u][i] > usobol[u][i])
  			{
  				System.out.println("lsobol greater than usobol: " + lsobol[u][i] + " > " + usobol[u][i]);
  				Assert.fail("lsobol greater than usobol: " + lsobol[u][i] + " > " + usobol[u][i]);
  			}
  		}
		}
		
		System.out.println("variable 1 lower Sobol=" + lowerSumm[0].getMean() + ", " + Math.sqrt(lowerSumm[0].getVariance()));
		System.out.println("variable 1 upper Sobol=" + upperSumm[0].getMean() + ", " + Math.sqrt(upperSumm[0].getVariance()));
		System.out.println("variable 2 lower Sobol=" + lowerSumm[1].getMean() + ", " + Math.sqrt(lowerSumm[1].getVariance()));
		System.out.println("variable 2 upper Sobol=" + upperSumm[1].getMean() + ", " + Math.sqrt(upperSumm[1].getVariance()));


	}

}
