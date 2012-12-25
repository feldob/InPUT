package miun.se.itm.input.example.performance.model;

import java.util.Random;

public class SomeJob implements Runnable {

	private static Random rng = new Random();
	
	private int firstValue;
	
	private Integer secondValue;
	
	private Option option;

	public SomeJob(int firstValue, Integer secondValue, Option option) {
		this.firstValue = firstValue;
		this.secondValue = secondValue;
		this.option = option;
	}
	
	@Override
	public void run() {
		// 1) check, is prime?
		int n = rng.nextInt(firstValue) + rng.nextInt(secondValue);
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