package se.miun.itm.input.test.app;

import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class Test {

	public Test() throws Exception {
		XMLFileExporter fileExp = new XMLFileExporter("something.xml");
		IDesignSpace space = new DesignSpace("realPSO.xml");
		IDesign design = space.nextDesign("test");

		design.export(fileExp);
		
		
		design = new Design("output.xml");
//		design.export(new XMLFileExporter("output2.xml"));
		
	}
	public static void main(String[] args) throws Exception {
		new Test();
	}
}