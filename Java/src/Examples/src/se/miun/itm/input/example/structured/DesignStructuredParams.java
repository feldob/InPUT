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
 */package se.miun.itm.input.example.structured;

import se.miun.itm.input.example.structured.model.AnotherDecision;
import se.miun.itm.input.example.structured.model.Decision;
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
 * That code mapping descriptor is never actively referred to in the implementation, all references are handled internally, via InPUT, given
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