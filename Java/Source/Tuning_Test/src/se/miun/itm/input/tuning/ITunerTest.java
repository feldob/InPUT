package se.miun.itm.input.tuning;

import java.io.File;

import org.junit.*;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.export.XMLArchiveExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class ITunerTest<SomeTuner extends ITuner> {

	protected SomeTuner tuner;

	protected static IInPUT input;

	private static final String TEST_FILE_NAME = "test.xml";

	protected InPUTExporter<?> exporter = new XMLArchiveExporter(TEST_FILE_NAME);

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
}