package miun.se.itm.input.example.performance;

import java.util.concurrent.ExecutionException;

import se.miun.itm.input.export.LaTeXFileExporter;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;

/**
 * A second extension of PerformanceAnalyzer, adding InPUT output, so that the
 * statistics are stored. The method calculateAndPrintStatistics has been
 * extended to export the results to both, latex and xml.
 * 
 * @author Felix Dobslaw
 * 
 */
public class PerformanceAnalyzer_InPUT2 extends PerformanceAnalyzer_InPUT1 {

	public PerformanceAnalyzer_InPUT2() throws InPUTException{
		super();
	}

	public static void main(String[] args) throws InPUTException,
			InterruptedException, ExecutionException {
		PerformanceAnalyzer_InPUT2 pa = new PerformanceAnalyzer_InPUT2();
		pa.analyze();
		pa.exportResults();
	}

	private void exportResults() throws InPUTException {
		IDesign results = new DesignSpace("performanceSpace.xml")
				.nextEmptyDesign("" + now()); // create an empty configuration
												// of type performanceSpace
		results.setValue("performance", runtime); // add the performance results
		results.export(new XMLFileExporter("performance.xml")); // export to XML
		results.export(new LaTeXFileExporter("performance.tex")); // export as
																	// LaTeX
																	// table
	}
}