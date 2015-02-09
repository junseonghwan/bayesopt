package design;

import java.util.Random;

import org.junit.Test;

public class LatinHypercubeSamplingTest {

	@Test
	public void test() {
		Random random = new Random(5);
		LatinHypercubeSampling.sample(random, 10, 3);
		
		// TODO: finish the test later
	}
}
