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
 */package miun.se.itm.input.example.performance.inject;

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