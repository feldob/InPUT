package miun.se.itm.input.example.performance;

import java.util.Calendar;
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

	public static void main(String[] args) throws ExecutionException, InterruptedException, InPUTException {
			new PerformanceAnalyzer_InPUT1().analyze();
	}

	public PerformanceAnalyzer_InPUT1() throws InPUTException, ExecutionException, InterruptedException {

		IDesign poolInvestigation = new Design("poolInvestigation.xml");
		amountTasks = poolInvestigation.getValue("amountTasks");
		task = poolInvestigation.getValue("task");
		executions = poolInvestigation.getValue("executions");
		poolSize = poolInvestigation.getValue("poolSize");
	}

	/**
	 * calculate sample mean and standard deviation for each thread pool size
	 * and print it to the standard output.
	 * 
	 * @param runtime
	 */
	protected void calculateAndPrintStatistics(long[][] runtime,
			int executions, int[] poolSizes) {
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

	private double sd(long[] observations, double mean) {
		double res = 0;
		for (int i = 0; i < observations.length; i++) {
			res += Math.pow(observations[i] - mean, 2);
		}

		return Math.sqrt((double) res / observations.length);
	}

	protected long now() {
		return Calendar.getInstance().getTimeInMillis();
	}
}