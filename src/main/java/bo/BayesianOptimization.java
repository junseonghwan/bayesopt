package bo;

import functions.Function;
import gp.GaussianProcess;

public class BayesianOptimization {
	
	/**
	 * 
	 * @param f - the function to find the minimum
	 * @param gp - GP with fixed Kernel parameter to be used for modelling f 
	 * @param maxIter - maximum number of iterations or budget
	 */
	public void minimize(Function f, GaussianProcess gp, int maxIter) {
		
		// 1. generate initial design points, D
		// 2. batch fit GP on these initial design points
		// 3. maximize EI criterion to find the next point, x,  to evaluate
		// 4. evaluate f(x), add (x, f(x)) to the training data, D' = D + (x, f(x))
		// 5. re-fit GP on D'
		// 6. repeat until budget is reached or the algorithm converged
	}

}
