package util;

import org.ejml.simple.SimpleMatrix;

public class EJMLUtil {

	public static SimpleMatrix rep(int d, double val) {
		SimpleMatrix vector = new SimpleMatrix(d, 1);
		for (int i = 0; i < d; i++)
		{
			vector.set(i, val);
		}
		return vector;
	}
}
