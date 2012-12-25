package se.miun.itm.input.model.design;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class DesignTest extends IDesignTest {

	@Before
	public void setUp() throws Exception {
		design = new Design(DESIGN_FILE);
	}

	@Test
	public void testDesign() {

		String[] flawedfileNames = {null, "someNotExistent.xml"};

		for (String fileName : flawedfileNames) {
			
			try {
				design = new Design(fileName);
				fail("The space file is inappropriate!");
			} catch (Exception e) {
			}
		}
	}
}
