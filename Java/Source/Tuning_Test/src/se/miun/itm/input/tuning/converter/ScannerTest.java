package se.miun.itm.input.tuning.converter;

import java.io.File;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

public class ScannerTest {

	private static final String SPOT_ROI = "numeric.roi";
	private Scanner scanner;
	
	@Before
	public void setUp() throws Exception {
		scanner = new Scanner(new File(SPOT_ROI));
	}

	@Test
	public void test() {
		while (scanner.hasNext()) {
			System.out.println(scanner.next());
		}
	}

}
