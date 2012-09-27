package miun.se.itm.input.example.performance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import miun.se.itm.input.example.performance.model.SomeJob;
import se.miun.itm.input.model.InPUTException;

/**
 * The performance analyzer using properties files. It requires the parsing of
 * the array entries and task property, so it requires problem specific code.
 * There is further no value validity ensurance. Explicit conversion is
 * required. The introduction of new task alternatives to SomeJob requires a
 * recompile. In principle, the same challenges have to be faced when using the
 * command line to read parameters.
 * 
 * @author Felix Dobslaw
 * 
 */
public class PerformanceAnalyzerProperties extends PerformanceAnalyzer {

	public PerformanceAnalyzerProperties() throws FileNotFoundException,
			IOException {

		Properties poolInvestiation = new Properties();
		poolInvestiation.load(new FileInputStream(
				"poolInvestigation.properties"));

		amountTasks = Integer.parseInt(poolInvestiation
				.getProperty("amountTasks"));

		String taskString = poolInvestiation.getProperty("task");
		task = initTask(taskString);

		executions = Integer.parseInt(poolInvestiation
				.getProperty("executions"));

		String[] poolString = poolInvestiation.getProperty("executions").split(
				",");
		poolSize = initPoolSize(poolString);
	}

	private int[] initPoolSize(String[] poolString) {
		int[] poolSize = new int[poolString.length];
		for (int i = 0; i < poolString.length; i++) {
			poolSize[i] = Integer.parseInt(poolString[i]);
		}
		return poolSize;
	}

	private Runnable initTask(String taskString) {
		if (taskString.equals("someJob")) {
			return new SomeJob();
		}
		throw new IllegalArgumentException(
				"The defined task type is not known by the source code :(");
	}

	public static void main(String[] args) throws InterruptedException,
			ExecutionException, InPUTException {
		new PerformanceAnalyzerPlain().analyze();
	}

}
