/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package se.miun.itm.input.model.design;

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
