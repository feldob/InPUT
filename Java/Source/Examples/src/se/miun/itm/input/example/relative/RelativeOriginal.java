package se.miun.itm.input.example.relative;

import java.util.Random;

public class RelativeOriginal {

	public static void main(String[] args) {
		Random rng = new Random();

		double a, b;

		a = rng.nextDouble();
		
		// say, a constraint is that the result of a quotient a/b -1 is always
		// positive, meaning that the second
		// random value depends on the first:
		b = rng.nextDouble() * (a);

		System.out.println((a / b) -1);
	}
}