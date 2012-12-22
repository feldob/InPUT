package se.miun.itm.input.example.result;

import java.math.BigDecimal;

import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * This example is an extension of the relative parameter definition example.
 * Say, the result of a calculation should be exported in order to be plotted,
 * or analyzed statistically. The export can easily be handled, using InPUT.
 * This example shows how a parameterized calculation is being repeated 100
 * times and exported to an output result space.
 * 
 * @author Felix Dobslaw
 * 
 */
public class ResultExample {

	public static void main(String[] args) throws InPUTException {
		IDesignSpace ds = new DesignSpace("relativeSpace.xml");

		BigDecimal[][] results = new BigDecimal[10][100];

		IDesign design;
		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < results[i].length; j++) {
				design = ds.nextDesign("someId");

				double a = design.getValue("a");
				double b = design.getValue("b");

				results[i][j] = new BigDecimal((a / b) - 1);
				System.out.println(results[i][j]);
			}
		}

		IDesignSpace resultSpace = new DesignSpace("resultSpace.xml");
		IDesign resultDesign = resultSpace.nextEmptyDesign("someResult");
		resultDesign.setValue("y", results);

		// export the output design to an xml
		resultDesign.export(new XMLFileExporter("randomDesignResults.xml"));
	}
}
