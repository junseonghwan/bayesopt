package functions;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.junit.Assert;
import org.junit.Test;

import bayonet.math.NumericalUtils;

public class OptimizerTest 
{
	@Test
	public void test() {
		MultivariateFunction branin = new Branin();
		SimplexOptimizer simplexOptimizer = new SimplexOptimizer(1e-6, 1e-6);
		
		PointValuePair solution = simplexOptimizer.optimize(new ObjectiveFunction(branin), new MaxEval(1000), GoalType.MINIMIZE, 
				new SearchInterval(0.0, 1.0), new InitialGuess(new double[]{0.5, 0.5}), new MultiDirectionalSimplex(2));
		
		double [] x = solution.getFirst();
		System.out.println("Minimum:" + solution.getValue() + " found at, (" + (x[0]*15 - 5) + ", " + 15*x[1] + ")");
		System.out.println("Difference to true value: " + (solution.getValue() - 0.397887));
		
		if (!NumericalUtils.isClose(solution.getValue(), 0.397887, 1e-6))
		{
			Assert.fail("Optimization did not work!");
		}
				
		
	}

}
