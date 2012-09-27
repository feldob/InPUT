package miun.se.itm.input.example.performance.model;

import java.util.Random;

public class SomeJob implements Runnable {

	private Random rng = new Random();

	@Override
	public void run() {
		// 1) calcuate prime
		int n = rng.nextInt(100000000) + rng.nextInt(4000000);
		boolean flag = true;
		for (int i = 2; i < n; i++) {
			if (n % i == 0)
				flag = false;
		}

		// 2) compete for IO
		if (flag)
			System.out.println("It's prime!");
		else
			System.out.println("No prime :(");

	}
}