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
 */package miun.se.itm.input.example.performance;

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