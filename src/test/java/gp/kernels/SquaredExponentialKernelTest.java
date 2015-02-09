package gp.kernels;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;
import org.junit.Assert;
import org.junit.Test;

import bayonet.math.NumericalUtils;
import gp.kernels.SquaredExponential;

public class SquaredExponentialKernelTest {

	@Test
	public void test() {
		SquaredExponential kernel = new SquaredExponential( new double[] {1.0, 0.5} );
		
		Random random = new Random(1234);
		
		double [] x = new double[2];
		x[0] = random.nextDouble();
		x[1] = random.nextDouble();
		
		double ret = kernel.value(x, x);
		if (ret != 1.0)
		{
			Assert.fail("Error in the kernel implementation");
		}
		
		x[0] = 2.7;
		x[1] = Math.PI;
		double [] y = new double[2];
		y[0] = 1.5;
		y[1] = 5.5;
		
		double ret2 = kernel.value(x, y);
		if (Math.abs(ret2 - 0.001869498) > 1e-6)
		{
			Assert.fail("Error in the kernel implementation");
		}
		
		double [][] xx = { {2.7, Math.PI}, {1.5, 5.5} };
		SimpleMatrix xxMat = new SimpleMatrix(xx);
		SimpleMatrix rMat = kernel.getCovarianceMatrix(xxMat);
		
		if( rMat.get(0, 0) != 1.0 || rMat.get(1,1) != 1.0 || 
				Math.abs(rMat.get(1,0) - 0.001869498) > 1e-6 ||
				Math.abs(rMat.get(0,1) - 0.001869498) > 1e-6 )
		{
			Assert.fail("Error in the kernel implementation");
		}
		
		
	}
}
