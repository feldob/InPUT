package se.miun.itm.input.example.structured;

import java.math.BigDecimal;
import java.math.RoundingMode;

import se.miun.itm.input.example.structured.model.AnotherDecision;
import se.miun.itm.input.example.structured.model.Raw;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class DesignStructuredParamsAdvanced {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("structuredSpaceAdvanced.xml");
		IDesign design = ds.nextDesign("someId");
		AnotherDecision choice = design.getValue("AnotherDecision");

		BigDecimal temperature = design.getValue("AnotherDecision.Temperature");

		System.out.println("Another decision: "
				+ choice.getClass().getSimpleName());

		System.out.println("Temperature: "
				+ temperature.setScale(3, RoundingMode.HALF_DOWN));

		if (choice instanceof Raw && temperature.doubleValue() < 30)
			System.out.println("Ohh this is no good :(");

		design.export(new XMLFileExporter("structuredAdvancedDesign.xml"));
	}
}
