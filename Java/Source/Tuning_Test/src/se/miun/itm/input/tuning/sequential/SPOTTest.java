package se.miun.itm.input.tuning.sequential;

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
		tuner = new SPOT(input, null, false);
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