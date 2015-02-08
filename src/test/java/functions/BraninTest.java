package functions;

import org.junit.Assert;
import org.junit.Test;

import bayonet.math.NumericalUtils;
import briefj.BriefIO;

public class BraninTest 
{
	
	@Test
	public void test() {
		
		int numLines = 0;
		for (String line : BriefIO.readLines("src/test/resources/branin-output.csv"))
		{
			numLines++;
		}
		
		double [] y = new double[numLines];
		double [][] x = new double[numLines][2];

		int lineno = 0;
		for (String line : BriefIO.readLines("src/test/resources/branin-output.csv"))
		{
			String [] l = line.split(",");
			x[lineno][0] = Double.parseDouble(l[0]);
			x[lineno][1] = Double.parseDouble(l[1]);
			y[lineno] = Double.parseDouble(l[2]);
			lineno++;
		}

		// compare it to the values that
		Function branin = new Branin();
		double [] yy = new double[numLines];
		for (int l = 0; l < numLines; l++)
		{
			yy[l] = branin.value(x[l]);
			
			if (!NumericalUtils.isClose(yy[l], y[l], 1e-4))
			{
				System.out.println(y[l] + " != " + yy[l]);
				Assert.fail("Error in Branin function implementation.");
			}
		}
		

	}
	

}
