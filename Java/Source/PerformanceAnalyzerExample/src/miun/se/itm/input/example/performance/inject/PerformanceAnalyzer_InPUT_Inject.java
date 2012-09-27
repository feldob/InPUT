package miun.se.itm.input.example.performance.inject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
public class PerformanceAnalyzer_InPUT_Inject {

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
		// no progamatic initialization necessary; descriptive via annotations instead.
	}

	@Set(value = "runtime", of = "performance", to = "runtime")
	protected void analyze() throws InterruptedException, ExecutionException {

		runtime = runExperiments(amountTasks, task, executions, poolSize);

		calculateAndPrintStatistics(runtime, executions, poolSize);
	}

	/**
	 * calculate sample mean and standard deviation for each thread pool size
	 * and print it to the standard output.
	 * 
	 * @param runtime
	 */
	private void calculateAndPrintStatistics(long[][] runtime, int executions,
			int[] poolSizes) {
		System.out.println();
		System.out.println("Results:");
		double sum;
		double mean, sd;
		for (int k = 0; k < runtime.length; k++) {
			sum = 0;
			for (int i = 0; i < runtime[k].length; i++) {
				sum += runtime[k][i];
			}
			mean = (double) sum / executions;
			sd = sd(runtime[k], mean);
			System.out.println("performance for a thread pool size of "
					+ poolSizes[k] + " is: " + mean + " +/- " + sd);
		}
	}

	protected long[][] runExperiments(int amountTasks, Runnable task,
			int executions, int[] poolSize) throws InterruptedException,
			ExecutionException {
		ExecutorService[] pools = initPoolsUnderInvestigation(poolSize);
		List<Integer> unorderedExperiments = initExperimentsInRandomOrder(
				poolSize.length, executions);

		Future<?>[] futures = new Future<?>[amountTasks];
		int[] experimentCounter = initializeExperimentCounters(poolSize.length);

		// create a container to store the performance results
		long[][] runtime = new long[poolSize.length][executions];

		long start;
		// for each experiment...
		for (Integer experimentId : unorderedExperiments) {
			// ... store start time ...
			start = now();

			// ... submit all tasks ...
			for (int j = 0; j < amountTasks; j++) {
				futures[j] = pools[experimentId].submit(task);
			}

			// ... wait till all tasks are finished...
			for (int j = 0; j < futures.length; j++) {
				futures[j].get();
			}

			// store the performance as an observation in the result container
			runtime[experimentId][experimentCounter[experimentId]] = now()
					- start;

			// print the result to the console
			System.out.println("thread pool size " + poolSize[experimentId]
					+ "; " + "experiment "
					+ (experimentCounter[experimentId] + 1) + "/" + executions
					+ "; execution time "
					+ runtime[experimentId][experimentCounter[experimentId]]
					+ ";");
			experimentCounter[experimentId]++;
		}
		return runtime;
	}

	private int[] initializeExperimentCounters(int amountPoolSizes) {
		int[] experimentCounter = new int[amountPoolSizes];
		for (int i = 0; i < experimentCounter.length; i++) {
			experimentCounter[i] = 0;
		}
		return experimentCounter;
	}

	private ExecutorService[] initPoolsUnderInvestigation(int[] poolSize) {
		ExecutorService[] pools = new ExecutorService[poolSize.length];

		for (int k = 0; k < pools.length; k++) {
			pools[k] = Executors.newFixedThreadPool(poolSize[k]);
		}
		return pools;
	}

	/**
	 * Experiments are defined in random order, so that the environmental noise
	 * is spread uniform at random.
	 */
	private List<Integer> initExperimentsInRandomOrder(int amountPoolSizes,
			int executions) {
		List<Integer> experiments = new ArrayList<Integer>();
		for (int i = 0; i < amountPoolSizes; i++) {
			for (int j = 0; j < executions; j++) {
				experiments.add(i);
			}
		}
		Collections.shuffle(experiments);
		return experiments;
	}

	private double sd(long[] observations, double mean) {
		double res = 0;
		for (int i = 0; i < observations.length; i++) {
			res += Math.pow(observations[i] - mean, 2);
		}

		return Math.sqrt((double) res / observations.length);
	}

	private long now() {
		return Calendar.getInstance().getTimeInMillis();
	}
}