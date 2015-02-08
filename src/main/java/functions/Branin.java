package functions;

import org.ejml.simple.SimpleMatrix;

public class Branin implements Function 
{
	
	public int getDim() { return 2; }

	/**
	 * Implemented as per http://www.sfu.ca/~ssurjano/branin.html
	 */
	@Override
  public double value(double[] x) {
		double x1 = 15*x[0] - 5;
		double x2 = 15*x[1];
		
		double a = 1.0, b = 5.1/(4*Math.pow(Math.PI, 2.0)), c = 5/Math.PI, r = 6, s = 10, t = 1/(8*Math.PI); 
		
		double term1 = x2 - b*Math.pow(x1, 2.0) + c*x1 - r;
		term1 = a*Math.pow(term1, 2.0);
		
		double term2 = s*(1 - t)*Math.cos(x1) + s;
		
		return term1 + term2;
  }

	/**
	 * For now, just call the the vector version of eval()
	 */
	@Override
  public double[] value(SimpleMatrix xx) {
		int n = xx.numRows();
		double [] retval = new double[n];
		for (int i = 0; i < n; i++)
		{
			retval[i] = value(xx.extractVector(true, i).getMatrix().getData());
		}
		
	  return retval;
  }

}
