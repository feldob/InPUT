package se.miun.itm.input.model.design;

import static org.junit.Assert.fail;

import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;

public class DesignSpaceTest extends IDesignSpaceTest {

	private String DESIGN_SPACE_MAPPING_FILE = "testSpaceMapping.xml";

	@Before
	public void setUp() throws Exception {
			space = new DesignSpace(DESIGN_SPACE_FILE);
	}

	@Test
	public void testDesignSpaceWithMappingAsInputStreams() {
		try {
			tearDown();
			space = new DesignSpace(new FileInputStream(DESIGN_SPACE_FILE),
					new FileInputStream(DESIGN_SPACE_MAPPING_FILE));
		} catch (Exception e) {
			fail("Both space and mapping entries should be settable via streams.");
		}
	}

	@Test
	public void testDesignSpaceFromFile() {
		try {
			tearDown();
			space = new DesignSpace(DESIGN_SPACE_MAPPING_FILE);
			fail("Mappings are not of appropriate structure for spaces.");
		} catch (Exception e) {
		}
	}

	@Test
	public void testDesignSpaceFromInputStream() {
		try {
			tearDown();
			space = new DesignSpace(new FileInputStream(DESIGN_SPACE_FILE));
		} catch (Exception e) {
			fail("Design spaces can be read as input streams.");
		}
	}
}
