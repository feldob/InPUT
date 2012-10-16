package se.miun.itm.input.example.structured;

import se.miun.itm.input.example.structured.model.Decision;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

/*
 * This example illustrates the use of InPUT for making random structural selections: here a variable is assigned an instance of
 * a randomly selected subclass in a hierarchy. This is accomplished with a mere few lines of code, where explicit references to 
 * the associated classes is rendered superfluous by the use of XML configuration files. As such, the program does not need to be 
 * modified and recompiled should the parameters change.
 * 
 * Parameters are described conceptually in the design space document, and the actual connection between a parameterized class name
 * and its Java implementation is made in the code mapping document (referred to from the design space). 
 * 
 * See also: https://github.com/feldob/InPUT/wiki/Structured-Random-Value-Creation
 * 
 */

public class StructuredRandomValueCreation {

	public static void main(String[] args) throws InPUTException {
		//Create design space: holds conceptual class descriptors & reference to code mapping
		IDesignSpace space = new DesignSpace("structuredSpaceBasic.xml");
		
		//Request a new instance of randomly chosen Decision-type subclass, as specified in design space
		Decision d = space.next("Decision");
		
		//Print name of selected class
		System.out.println(d.getClass().getName());
	}
}
