package functions;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.ejml.simple.SimpleMatrix;

public interface Function extends MultivariateFunction
{
	
	public int getDim();

	/**
	 * Vector version of the function evaluation
	 * Evaluate the function at x
	 * @param x
	 * @return
	 */
	public double value(double [] x);
	
	/**
	 * Matrix version of the function evaluation
	 * @param xx - n x d array where each row represents an input to the function
	 * @return
	 */
	public double [] value(SimpleMatrix xx);
	
}
