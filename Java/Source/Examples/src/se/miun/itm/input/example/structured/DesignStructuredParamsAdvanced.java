package se.miun.itm.input.example.structured;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

import se.miun.itm.input.example.structured.model.AnotherDecision;
import se.miun.itm.input.example.structured.model.Raw;
import se.miun.itm.input.export.InputProperties;
import se.miun.itm.input.export.LaTeXFileExporter;
import se.miun.itm.input.export.PropertiesExporter;
import se.miun.itm.input.export.XMLArchiveExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public class DesignStructuredParamsAdvanced {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("structuredSpaceAdvanced.xml");
		IDesign design = ds.nextDesign("someId");
		AnotherDecision choice = design.getValue("Steak");

		BigDecimal temperature = design.getValue("Steak.Temperature");

		System.out.println("Steak: "
				+ choice.getClass().getSimpleName());

		System.out.println("Temperature: "
				+ temperature.setScale(3, RoundingMode.HALF_DOWN));

		if (choice instanceof Raw && temperature.doubleValue() < 30)
			System.out.println("Ohh this is no good :(");

		design.export(new XMLArchiveExporter("structuredAdvancedDesign.xml"));
		design.export(new LaTeXFileExporter("t.tex"));
	}
}
