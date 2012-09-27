package se.miun.itm.input.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.export.ZipFileExporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.impOrt.InPUTArchiveImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class InPUTExperimentTest {

	private IInPUT input;

	@Before
	public void setUp() throws InPUTException, FileNotFoundException {
		input = new InPUT("theId", "geneticAlgorithm.xml", null, null,
				"outputSpace.xml");
	}

	@Test
	public void testImportExport() throws InPUTException {
		IExperiment experiment = new Experiment("1", input);
		IDesignSpace outputSpace = input.getOutputSpace();
		IDesign output1 = outputSpace.nextDesign("test1", true);
		IDesign output2 = outputSpace.impOrt(new XMLFileImporter("output.xml"));
		experiment.addOutput(output1);
		experiment.addOutput(output2);
		experiment.export(new ZipFileExporter("theExperiment.zip"));
		assertTrue(new File("theExperiment.zip").exists());
		input.export(new ZipFileExporter("theExperimentSetup.zip"));
		assertTrue(new File("theExperimentSetup.zip").exists());

		IInPUT input2 = InPUT.getInPUT(new InPUTArchiveImporter("id",
				"theExperimentSetup.zip"));
		ExperimentArchiveImporter importer2 = new ExperimentArchiveImporter(
				"theExperiment.zip");
		experiment.impOrt(importer2);

		new File("theExperiment.zip").delete();
		new File("theExperimentSetup.zip").delete();
	}
}