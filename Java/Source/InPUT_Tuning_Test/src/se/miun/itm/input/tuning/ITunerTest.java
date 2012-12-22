package se.miun.itm.input.tuning;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.export.Exporter;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class ITunerTest<SomeTuner extends ITuner> {

	protected SomeTuner tuner;
	
	protected static IInPUT input;
	
	private static final String TEST_FILE_NAME = "test.xml";

	protected Exporter<?> exporter = new XMLFileExporter(TEST_FILE_NAME);

	@Before
	public void setUp() throws InPUTException {
		IDesignSpace algorithmSpace = new DesignSpace("combinedSpace.xml");
		IDesignSpace propertySpace = new DesignSpace("numericalSpace.xml");
		IDesignSpace problemSpace = new DesignSpace("structuralSpace.xml");
		input = new InPUT(algorithmSpace.getId(), algorithmSpace, propertySpace, problemSpace, Tuner.getSingleObjectiveSpace());
	}
	
	@After
	public void tearDown() throws InPUTException {
		new File(TEST_FILE_NAME).delete();
	}

	@Test
	public void testGetExperimentalDesign() throws InPUTException {
		List<IExperiment> designs = tuner.nextIteration();

		assertEquals(12, designs.size());

		for (IExperiment design : designs) {
			design.getAlgorithmDesign().export(exporter);
			new Design(TEST_FILE_NAME);
		}
	}
}