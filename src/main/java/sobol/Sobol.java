package sobol;
import java.util.Arrays;

public class Sobol 
{
	public double lower( Func func, double[][] xx, double[][] zz, int[] u, double[] args ) throws Exception
	{
		if( xx.length != zz.length )
		{
			throw new RuntimeException();
		}
		
		int l = xx.length;
			
		for( int i : u )
		{
			for( int j = 0; j < l; j++ )
			{
				zz[j][i] = xx[j][i];
			}
		}
		
		double[] fxx = func.fun( xx, args );
		double[] fzz = func.fun( zz, args );
		
		double mu_hat = 0;
		double cross_moment = 0;
		for( int i = 0; i < l; i++ )
		{
			mu_hat += ( fxx[i] + fzz[i] ) / ( 2 * l );
			cross_moment += ( fxx[i] * fzz[i] ) / l;
		}
		
		return cross_moment - mu_hat * mu_hat;
	}
	
	public double upper( Func func, double[][] xx, double[][] zz, int[] u, double[] args ) throws Exception
	{
		if( xx.length != zz.length )
		{
			throw new RuntimeException();
		}
		
		int l = xx.length;
		int d = xx[0].length;
		
		for( int i = 0; i < d; i++ )
		{
			if(  )
			{
				for( int j = 0; j < l; j++ )
				{
					zz[j][i] = xx[j][i];
				}
			}
		}
		
		double[] fxx = func.fun( xx, args );
		double[] fzz = func.fun( zz, args );
		
		double mu_hat = 0;
		for( int i = 0; i < l; i++ )
		{
			mu_hat += ( fxx[i] - fzz[i] ) * ( fxx[i] - fzz[i] ) / ( 2 * l );
		}
		
		return mu_hat;
	}
	
}
