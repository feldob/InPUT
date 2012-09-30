package se.miun.itm.input.test.app;

import java.util.Calendar;
import java.util.Random;

import org.uncommons.maths.random.MersenneTwisterRNG;

public class SimpleRandomizer {

	public static void main(String[] args) {

		long start = Calendar.getInstance().getTimeInMillis();

		Random rng = new MersenneTwisterRNG();
		double a, b;
		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < 100; i++) {
				a = rng.nextDouble();
				b = .2 + (rng.nextDouble() * (.8 - .2));
				a = a * b;
			}
		}
		System.out.println((Calendar.getInstance().getTimeInMillis() - start));
	}
}