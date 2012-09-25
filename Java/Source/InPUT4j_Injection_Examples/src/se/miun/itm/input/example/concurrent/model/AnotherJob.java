package se.miun.itm.input.example.concurrent.model;

import java.util.Random;

public class AnotherJob implements Runnable {

	private static Random rng = new Random();
	
	@Override
	public void run() {
			double x = Math.sqrt(rng.nextInt())/Math.sqrt(rng.nextInt()) + Math.sqrt(rng.nextInt())/Math.sqrt(rng.nextInt());
	}
}