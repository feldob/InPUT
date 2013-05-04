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
 */package se.miun.itm.input.example.inputoutput;

import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * An example that shows how InPUT can be used to 1) read a design space file, 2) import
 * a design file of that type, 3) run some experiment, and 4) output the results.
 * 
 * @author Felix Dobslaw
 * 
 */
public class InputOutputTraditional {

	public static void main(String[] args) throws InPUTException {
		new InputOutputTraditional();
	}

	public InputOutputTraditional() throws InPUTException {
		IDesignSpace someSpace = new DesignSpace("someSpace.xml");
		IDesign inputDesign = someSpace
				.impOrt(new XMLFileImporter("input.xml"));

		doSomething(someSpace, inputDesign);
	}

	private void doSomething(IDesignSpace space, IDesign inputDesign)
			throws InPUTException {
		int[] input = inputDesign.getValue("paramId");

		int[] output = new int[input.length];

		for (int i = 0; i < input.length; i++) {
			System.out.println(input[i]);
			output[i] = input[i];
		}

		IDesign outputDesign = space.nextEmptyDesign("outputId");
		outputDesign.setValue("paramId", output);
		outputDesign.attachEnvironmentInfo();

		outputDesign.export(new XMLFileExporter("output.xml"));
	}
}