package se.miun.itm.input.example.relative;

import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * The quotient restriction is handled via the descriptor
 * @author Felix Dobslaw
 *
 */
public class RelativeInPUT {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("relativeSpace.xml");
		IDesign design = ds.nextDesign("someId");

		double a = design.getValue("a");
		double b = design.getValue("b");

		System.out.println((a / b) - 1);
		
		// export the design to an xml
		design.export(new XMLFileExporter("randomDesign.xml"));
	}
}