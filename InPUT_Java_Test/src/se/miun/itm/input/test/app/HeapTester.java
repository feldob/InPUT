package se.miun.itm.input.test.app;

import java.io.FileNotFoundException;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class HeapTester {

	public static final String METAINPUT_XML_TESTFILE = "geneticAlgorithm.xml";
	public static String DSL_FILE = "geneticAlgorithmMapping.xml";

	public static void main(String[] args) throws InPUTException,
			FileNotFoundException {
		IDesignSpace designSpace;
		int counter = 0;
		System.out.println("init");
		while (true) {
			designSpace = new DesignSpace(METAINPUT_XML_TESTFILE);
			System.out.println("next round:");
			for (int i = 0; i < 100; i++) {
				designSpace.nextDesign("selection");
				System.out.println(counter);
				counter++;
			}
		}
	}
}