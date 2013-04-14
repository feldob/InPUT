package se.miun.itm.input.example.externalize;

import se.miun.itm.input.export.XMLArchiveExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * This class extends the relative param definition example, by illustrating how a
 * constant decision can be externalized as a parameter, using an existing InPUT descriptor.
 * This can be valuable, if you right now want to keep a parameter fixed, but might want to
 * play with it in a later stage.
 * 
 * Starting from the example in RelativeInPUT, you have to:
 * 1) identify the constant to be extracted (the '1' in the formula)
 * 2) substitute the constant with a variable identifier (e.g. "c")
 * 3) add the variable retrieval  with the identifier using InPUT to the code.
 * 4) add the variable as a parameter to the design space. If desired, it can be
 * set to a fixed value, in order to have it specified as a degree that could easily be
 * adjusted in a later stage.
 * 
 * @author Felix Dobslaw
 *
 */
public class ExternalizeFixedParam {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("externalizedSpace.xml");
		IDesign design = ds.nextDesign("someOtherId");

		double a = design.getValue("a");
		double b = design.getValue("b");
		double c = design.getValue("c");
		
		System.out.println((a / b) - c);
		
		// export the design to an xml
		design.export(new XMLArchiveExporter("randomDesign.xml"));
	}
}
