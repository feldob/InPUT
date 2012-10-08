package se.miun.itm.input.example.structured;

import se.miun.itm.input.example.structured.model.Decision;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class StructuredRandomValueCreation {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace space = new DesignSpace("structuredSpaceBasic.xml");
		Decision d = space.next("Decision");
		System.out.println(d.getClass().getName());
	}
}
