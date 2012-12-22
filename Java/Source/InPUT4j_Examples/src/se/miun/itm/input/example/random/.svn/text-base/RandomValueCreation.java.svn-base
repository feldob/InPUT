package se.miun.itm.input.example.random;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class RandomValueCreation {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("someSpace.xml");
		IDesign design = ds.nextDesign("someId");
		
		int[] values = design.getValue("paramId");
		
		for (int i = 0; i < values.length; i++) {
			System.out.println(values[i]);
		}
	}
	
	public RandomValueCreation() throws InPUTException {
	}
}
