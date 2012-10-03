package se.miun.itm.input.test.app;

/**
 * This example shows an easy way to randomly select a random number.
 * The random number is selected according to how long it takes to select the product of 
 * the two randomly selected factors a and b. There is no directy correlance between the size of 
 * the intervall (the values in the xml-file) and the randomly selected number. 
 * 
 */


import java.io.FileNotFoundException;
import java.util.Calendar;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class InPUTRandomizer {

	public static void main(String[] args) throws InPUTException, FileNotFoundException {

		IDesignSpace input = new DesignSpace("app.xml");

		long start = Calendar.getInstance().getTimeInMillis();
		double a,b;
		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < 100; i++) {
				a = input.next("a");
				b = input.next("b");
				a= a * b;
			}
		}
		System.out.println((Calendar.getInstance().getTimeInMillis() - start));
	}
}