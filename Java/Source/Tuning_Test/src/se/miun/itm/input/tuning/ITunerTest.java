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
 */package se.miun.itm.input.tuning;

import java.io.File;

import org.junit.*;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class ITunerTest<SomeTuner extends ITuner> {

	protected SomeTuner tuner;

	protected static IInPUT input;

	private static final String TEST_FILE_NAME = "test.xml";

	protected InPUTExporter<?> exporter = new XMLFileExporter(TEST_FILE_NAME);

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