package gp;

import briefj.run.Mains;

public class GPFit implements Runnable
{
	
	public void run() {
		
	}
	
	public static void main(String [] args) {
		Mains.instrumentedRun(args, new GPFit());
	}

}
