package bo;

import java.util.Random;

import org.ejml.simple.SimpleMatrix;

import design.LatinHypercubeSampling;
import functions.Function;
import gp.GaussianProcess;
import gp.kernels.Kernel;
import gp.kernels.SquaredExponential;

public class BayesianOptimization {
	
	/**
	 * 
	 * @param f - the function to find the minimum
	 * @param gp - GP with fixed Kernel parameter to be used for modelling f 
	 * @param maxIter - maximum number of iterations or budget
	 */
	public void minimize(Random random, Function f, int n0, int maxIter) {
		
		// 1. generate initial design points, D and evaluate f(D)
		// 2. batch fit GP on these initial design points
		// 3. maximize EI criterion to find the next point, x,  to evaluate
		// 4. evaluate f(x), add (x, f(x)) to the training data, D' = D + (x, f(x))
		// 5. re-fit GP on D'
		// 6. repeat until budget is reached or the algorithm converged
		
		double [][] X = LatinHypercubeSampling.sample(random, n0, f.getDim());
		SimpleMatrix xMat = new SimpleMatrix(X);
		double [] y = f.value(xMat);
		
		int d = f.getDim();
		double [] initialTheta = new double[d];
		for (int i = 0; i < d; i++) {
			initialTheta[i] = random.nextDouble();
		}
		
		Kernel kernel = new SquaredExponential(initialTheta);
		
		
		// maximize EI criterion
		
		
	}

}
