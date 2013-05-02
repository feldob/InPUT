package test;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;

public class TheMain {

	public static void main(String[] args) throws InPUTException {
		IDesign config = new Design("config.xml");
		ListCreator[] creator = config.getValue("ListCreator");
//		ListCreator creator = config.getValue("ListCreator");
		
		System.out.println(config);
	}
}
