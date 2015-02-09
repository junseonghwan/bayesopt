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

		SimpleMatrix xvec = new SimpleMatrix(2, 1, false, x);
		double ret = kernel.value(xvec, xvec);
		if (ret != 1.0)
		{
			Assert.fail("Error in the kernel implementation");
		}
		
		xvec.set(0, 2.7);
		xvec.set(1, Math.PI);
		double [] y = new double[2];
		y[0] = 1.5;
		y[1] = 5.5;
		SimpleMatrix yvec = new SimpleMatrix(2, 1, false, y);
		
		double ret2 = kernel.value(xvec, yvec);
		//System.out.println(ret2 - 0.01468333);
		if (Math.abs(ret2 - 0.01468333) > 1e-6)
		{
			Assert.fail("Error in the kernel implementation");
		}
		
		double [][] xx = { {2.7, Math.PI}, {1.5, 5.5} };
		SimpleMatrix xxMat = new SimpleMatrix(xx);
		SimpleMatrix rMat = kernel.getCovarianceMatrix(xxMat);
		
		if( rMat.get(0, 0) != 1.0 || rMat.get(1,1) != 1.0 || 
				Math.abs(rMat.get(1,0) - 0.01468333) > 1e-6 ||
				Math.abs(rMat.get(0,1) - 0.01468333) > 1e-6 )
		{
			Assert.fail("Error in the kernel implementation");
		}
		
		
	}
}
