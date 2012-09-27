package miun.se.itm.input.example.performance;

import java.util.concurrent.ExecutionException;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;

/**
 * An first extension of PerformanceAnalyzer, using InPUT for the inclusion of
 * parameter decisions for configuration.
 * 
 * @author Felix Dobslaw
 * 
 */
public class PerformanceAnalyzer_InPUT1 extends PerformanceAnalyzerPlain {

	public PerformanceAnalyzer_InPUT1() throws InPUTException, ExecutionException, InterruptedException {

		IDesign poolInvestigation = new Design("poolInvestigation.xml");
		amountTasks = poolInvestigation.getValue("amountTasks");
		task = poolInvestigation.getValue("task");
		executions = poolInvestigation.getValue("executions");
		poolSize = poolInvestigation.getValue("poolSize");
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException, InPUTException {
			new PerformanceAnalyzer_InPUT1().analyze();
	}
}