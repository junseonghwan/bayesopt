package bo;

import java.util.Random;

import functions.Branin;
import functions.Function;
import briefj.run.Mains;

public class BraninBO implements Runnable 
{

	public void run() {
		BayesianOptimization bo = new BayesianOptimization();
		Random random = new Random(55);
		Function branin = new Branin();
		bo.minimize(random, branin, 10, 100);
	}
	
	public static void main(String [] args) {
		Mains.instrumentedRun(args, new BraninBO());
	}
	

}
