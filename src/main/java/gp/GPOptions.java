package gp;

import java.util.Random;

public class GPOptions {

	public static boolean verbose = false;
	public static double searchLowerBound = 0.01;
	public static double searchUpperBound = 50;
	public static int numRestarts = 20;
	public static Random random = new Random(1);
	
	public static double [] randomGuess(int d) {
		double [] guess = new double[d];
		for (int i = 0; i < d; i++) {
			guess[i] = random.nextDouble()*GPOptions.searchUpperBound;
		}
		return guess;
	}
}
