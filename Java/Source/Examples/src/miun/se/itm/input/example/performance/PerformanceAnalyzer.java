package miun.se.itm.input.example.performance;

import java.util.concurrent.ExecutionException;

/**
 * An demo program that can be used to identify an appropriate thread pool size.
 * Formulas for appropriate sizes exist, depending on the amount of available
 * cores, but this code is the base for an example to illustrate the features of
 * the Intelligent Parameter Utilization Tool (InPUT). We assume a repetitive
 * task (example: SomeJob), its workload (amountTasks), and the thread pool
 * sizes (poolSize) to be tested.
 * 
 * @author Felix Dobslaw
 * 
 */
public abstract class PerformanceAnalyzer extends AbstractPerformanceAnalyzer {

	// amount of concurrent tasks
	protected int amountTasks;
	
	// the repetitive task
	protected Runnable task;
	
	// amount of repeated experiments: the more, the more precise the result
		// estimate (central limit theorem)
	protected int executions;
	
	// the different pool sizes to be tested
	protected int[] poolSize; 
	
	// the performance results
	protected long[][] runtime;
		
	protected void analyze() throws InterruptedException, ExecutionException {

		runtime = runExperiments(amountTasks, task, executions,
				poolSize);

		calculateAndPrintStatistics(runtime, executions, poolSize);
	}
}