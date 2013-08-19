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
 */package se.miun.itm.input.sql.example.dynamic;

import se.miun.itm.input.junit.DatabaseConnectingDialog;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.sql.DatabaseAdapter;

public class DesignManipulation {

	public static void main(String[] args) throws InPUTException {
		IDesign design = new DatabaseAdapter(DatabaseConnectingDialog.connect()).
			importDesign("1");
		IDesignSpace space = design.getSpace();
		
		int[] param;
		
		for (int i = 0; i < 10; i++) {
			param = space.next("paramId");
			design.setValue("paramId", param);
			System.out.println("current design values:");
			for (int j = 0; j < param.length; j++) {
				System.out.println(param[j]);
				//test your algorithm here with the parameters as input, and record your statistics
			}
			System.out.println("---------------------\n");
		}
		
	}
	
	
}
