package bo;

import java.util.Random;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;

import util.StandardDistributionUtils;
import gp.GaussianProcess;

/**
 * Expected improvement based on Gaussian process
 * @author Seong-Hwan Jun (s2jun.uw@gmail.com)
 *
 */
public class ExpectedImprovement
{
	private static SearchInterval bounds = new SearchInterval(0.0, 1.0);

	public ExpectedImprovement() {
	}
	
	public PointValuePair nextPoint(Random random, GaussianProcess gp, double fmin) {
		// evaluate E[max(f_min - f(x), 0)] = (f_min - y) \Phi((f_min - y)/s) + s \phi((f_min - y)/s)

		MultivariateFunction eiFunc = new MultivariateFunction() {
			@Override
			public double value(double[] point) {
				return evaluate(point, gp, fmin);
			}
		};

		// optimize evaluate function
		double maxEI = Double.NEGATIVE_INFINITY;
		PointValuePair opt = null;
		int d = gp.getDim();
		int numTries = 10*d;
		for (int i = 0; i < numTries; i++) {
	  		SimplexOptimizer simplexOptimizer = new SimplexOptimizer(1e-4, 1e-6);
	  		PointValuePair solution = simplexOptimizer.optimize(new ObjectiveFunction(eiFunc), 
	  				new MaxEval(100000), GoalType.MAXIMIZE, bounds, 
	  				new InitialGuess(randomPoints(random, d)), new NelderMeadSimplex(gp.getDim()));
	  		
	  		if (solution.getValue() > maxEI) {
	  			maxEI = solution.getValue();
	  			opt = solution;
	  		}
		}
		//System.out.println("EI=" + opt.getValue());

	  return opt;
  }
	
	public double evaluate(double [] point, GaussianProcess gp, double fmin) {
		for (int i = 0; i < point.length; i++) {
			if (point[i] < 0.0 || point[i] > 1.0) {
				return Double.NEGATIVE_INFINITY;
			}
		}

		double [] pred = gp.predict(point);
		double yhat = pred[0];
		double s = Math.sqrt(pred[1]);
		double val = (fmin-yhat)/s;
		double ret = (fmin - yhat)*StandardDistributionUtils.stdNormal.cumulativeProbability(val) + s*StandardDistributionUtils.stdNormal.density(val);
		return ret;
	}
	
	private double [] randomPoints(Random random, int d) 
	{
		double [] randomPoints = new double[d];
		for (int i = 0; i < d; i++) {
			randomPoints[i] = random.nextDouble();
		}
		return randomPoints;
	}

	
}
