package sobol;

public class Func {
	public double[] fun( double[][] x, double[] args )
	{
		double a = args[0];
		double b = args[1];
		double c = args[2];
		double r = args[3];
		double s = args[4];
		double t = args[5];

		int l = x.length;
		
		double[] y = new double[l];
		double term1, term2;
		
		for( int i = 0; i < l; i++ )
		{
			term1 = a * Math.pow( x[i][1] - b * x[i][0] * x[i][0] + c * x[i][0] - r, 2 );
			term2 = s * ( 1 - t ) * Math.cos( x[i][0] );
			y[i] = term1 + term2 + s;
		}
		
		return y;
	}
}
