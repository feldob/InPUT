package se.miun.itm.input.example.inputoutput;

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
		Object[] input = inputDesign.getValue("paramId");

		Object[] output = new Object[input.length];

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