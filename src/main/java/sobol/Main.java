package sobol;
import java.io.*;
import java.util.Arrays;

public class Main {
	public static void main( String[] args ) throws Exception
	{
		double x[][] = { {0, 1}, {2, 3}, {4, 5} };
		double Args[] = { 1, 5.1 / ( 4 * Math.PI * Math.PI ), 5 / Math.PI, 6, 10, 1 / ( 8 * Math.PI ) };
		Func func = new Func();
		double y[] = func.fun(x, Args);
		for( int i = 0; i < y.length; i++ )
		{
			System.out.println(y[i]);
		}
		System.out.println( "Test sobol upper and lower" );
		double z[][] = { {6, 7}, {8, 9}, {10, 11} };
		int u[] = { 1 };
		
		Sobol sobol = new Sobol();
		double low = sobol.lower( func, x, z, u, Args );
		double up = sobol.upper( func, x, z, u, Args );
		System.out.println( "UPPER = " + up );
		System.out.println( "LOWER = " + low );
	}
}
