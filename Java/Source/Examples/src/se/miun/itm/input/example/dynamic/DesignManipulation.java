package se.miun.itm.input.example.dynamic;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class DesignManipulation {

	public static void main(String[] args) throws InPUTException {
		IDesign design = new Design("input.xml");
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
