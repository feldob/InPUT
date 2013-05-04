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
 */package se.miun.itm.input.example.externalize;

import se.miun.itm.input.export.XMLFileExporter;
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
		design.export(new XMLFileExporter("randomDesign.xml"));
	}
}
