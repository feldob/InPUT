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
 */package se.miun.itm.input.tuning.sequential;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import se.miun.itm.input.model.InPUTException;

public class SPOTTest extends ISequentialTunerTest {
	
	private ExperimentFilter filter = new ExperimentFilter();
	
	class ExperimentFilter implements FilenameFilter{

		@Override
		public boolean accept(File dir, String name) {
			if (name.startsWith("experiment"))
				return true;
			return false;
		}
	}
	@Before
	public void setUp() throws InPUTException {
		super.setUp();
		tuner = new SPOT(input, null, null,"testStudy", false, false);
	}

	@After
	public void tearDown() throws InPUTException {
		super.tearDown();
		File parentFolder = new File(".");
		if (parentFolder.exists()) {
		for (File exp : parentFolder.listFiles(filter))
			try {
				delete(exp);
			} catch (IOException e) {
				throw new InPUTException(e.getMessage(), e);
			}
		}
	}

	void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
	}
}