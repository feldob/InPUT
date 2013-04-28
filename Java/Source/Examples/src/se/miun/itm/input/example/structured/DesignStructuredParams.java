package se.miun.itm.input.example.structured;

import se.miun.itm.input.example.structured.model.AnotherDecision;
import se.miun.itm.input.example.structured.model.Decision;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * Structured parameter types is where InPUT really makes a difference. It allows
 * the user to define structural choices, meaning fully initialized instances of alternative objects, to be returned. 
 * 
 * This, however, requires a third descriptor concept, on top of the design space, and design descriptors, namely, the code mappings.
 * The code mapping lets a user define the valid alternative choices for a parameter. Those are first conceptually defined in the
 * design space, and have to be enhanced in a code mapping xml descriptor, to match the implementation.
 * That code mapping descriptor is never actively refered to in the implementation, all references are handled internally, via InPUT, given
 * that the descriptor reference attribute "mapping" points to the correct file.
 *  
 * @author Felix Dobslaw
 *
 */
public class DesignStructuredParams {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("structuredSpaceBasic.xml");
		IDesign design = ds.nextDesign("someId");
		Decision choice1 = design.getValue("Decision");
		AnotherDecision choice2 = design.getValue("AnotherDecision");
		
		System.out.println("Decision: " + choice1.getClass().getSimpleName());
		System.out.println("Another decision: " + choice2.getClass().getSimpleName());

	}
}