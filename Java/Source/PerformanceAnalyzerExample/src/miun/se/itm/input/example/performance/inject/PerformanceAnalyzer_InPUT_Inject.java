package miun.se.itm.input.example.performance.inject;

import java.util.concurrent.ExecutionException;

import miun.se.itm.input.example.performance.AbstractPerformanceAnalyzer;
import se.miun.itm.input.annotation.Get;
import se.miun.itm.input.annotation.Input;
import se.miun.itm.input.annotation.Output;
import se.miun.itm.input.annotation.Set;
import se.miun.itm.input.model.InPUTException;

/**
 * An extension of the PerformanceAnalyzer, using the simplified injection based
 * parameter access.
 * 
 * @author Felix Dobslaw
 * 
 */
public class PerformanceAnalyzer_InPUT_Inject extends
		AbstractPerformanceAnalyzer {

	// amount of concurrent tasks
	@Get(value = "amountTasks", from = "poolInvestigation")
	private int amountTasks;

	// the repetitive task
	@Get(value = "task", from = "poolInvestigation")
	private Runnable task;

	// amount of repeated experiments: the more, the more precise the result
	// estimate (central limit theorem)
	@Get(value = "executions", from = "poolInvestigation")
	private int executions;

	// the different pool sizes to be tested
	@Get(value = "poolSize", from = "poolInvestigation")
	private int[] poolSize;

	// the performance results
	private long[][] runtime;

	public static void main(String[] args) throws InterruptedException,
			ExecutionException, InPUTException {
		new PerformanceAnalyzer_InPUT_Inject().analyze();
	}

	@Input(id = "poolInvestigation", file = "poolInvestigation.xml")
	@Output(id = "performance", file = "performance.xml", spaceFile = "performanceSpace.xml")
	public PerformanceAnalyzer_InPUT_Inject() {
		// no boilerplate initialization necessary
	}

	@Set(value = "performance", of = "performance", to = "runtime")
	protected void analyze() throws InterruptedException, ExecutionException {

		runtime = runExperiments(amountTasks, task, executions, poolSize);

		calculateAndPrintStatistics(runtime, executions, poolSize);
	}
}