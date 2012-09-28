package miun.se.itm.input.example.performance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import miun.se.itm.input.example.performance.model.Option;
import miun.se.itm.input.example.performance.model.SomeJob;
import miun.se.itm.input.example.performance.model.SomeOption;
import se.miun.itm.input.model.InPUTException;

/**
 * The performance analyzer using properties files. It requires the parsing of
 * the array entries and task property, so it requires problem specific code.
 * There is further no value validity ensurance. Explicit conversion is
 * required. The introduction of new task alternatives to SomeJob requires a
 * recompile. In principle, the same challenges have to be faced when using the
 * command line to read parameters. Sub parameters have to be entirely unfolded and
 * the different choices have to be checked.
 * 
 * @author Felix Dobslaw
 * 
 */
public class PerformanceAnalyzerProperties extends PerformanceAnalyzer {

	public PerformanceAnalyzerProperties() throws FileNotFoundException,
			IOException {

		Properties poolInvestigation = new Properties();
		poolInvestigation.load(new FileInputStream(
				"poolInvestigation.properties"));

		amountTasks = Integer.parseInt(poolInvestigation
				.getProperty("amountTasks"));

		task = initTask(poolInvestigation);

		executions = Integer.parseInt(poolInvestigation
				.getProperty("executions"));

		String[] poolString = poolInvestigation.getProperty("executions").split(
				",");
		poolSize = initPoolSize(poolString);
	}

	public static void main(String[] args) throws InterruptedException,
			ExecutionException, InPUTException {
		new PerformanceAnalyzerPlain().analyze();
	}

	private int[] initPoolSize(String[] poolString) {
		int[] poolSize = new int[poolString.length];
		for (int i = 0; i < poolString.length; i++) {
			poolSize[i] = Integer.parseInt(poolString[i]);
		}
		return poolSize;
	}

	private Runnable initTask(Properties poolInvestigation) {
		String taskString = poolInvestigation.getProperty("task");
		if (taskString.equals("someJob")) {
			int firstValue = Integer.parseInt(poolInvestigation.getProperty("someJob.firstValue"));
			int secondValue = Integer.parseInt(poolInvestigation.getProperty("someJob.secondValue"));
		
			String optionString = poolInvestigation.getProperty("someJob.option");
			if (optionString.equals("someOption")) {
				Option option = new SomeOption();
				return new SomeJob(firstValue, secondValue, option);
			}
			throw new IllegalArgumentException(
					"The defined option type is not known by the source code :(");
		}
		throw new IllegalArgumentException(
				"The defined task type is not known by the source code :(");
	}

}
