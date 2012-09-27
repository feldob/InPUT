package se.miun.itm.input.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.export.ZipFileExporter;
import se.miun.itm.input.model.design.IDesignSpace;

public class InPUTTest {

	private IInPUT input;

	@Before
	public void setUp() throws Exception {
		input = new InPUT("someId", DesignSpaceTest.METAINPUT_XML_TESTFILE,
				null, null, null);
	}

	@Test
	public void testWriteInPUT() {
		boolean flag = true;
		try {
			IDesignSpace aSpace = input.getAlgorithmDesignSpace();
			IExperiment experiment = new Experiment("1", input); 
			experiment.setAlgorithmDesign(aSpace.nextDesign("simplythebest"));
			input.export(new ZipFileExporter("inputTest"));
			experiment.export(new ZipFileExporter("experimentTest"));
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		assertTrue(flag);
		new File("test.zip").delete();
		new File("experimentTest.zip").delete();
		new File("inputTest.zip").delete();
	}

}
