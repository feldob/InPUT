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