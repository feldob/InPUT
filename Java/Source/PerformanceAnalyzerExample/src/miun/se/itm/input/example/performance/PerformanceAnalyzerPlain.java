package miun.se.itm.input.example.performance;

import java.util.concurrent.ExecutionException;

import miun.se.itm.input.example.performance.model.SomeJob;
import miun.se.itm.input.example.performance.model.SomeOption;
import se.miun.itm.input.model.InPUTException;

/**
 * An implementation not using any parameter import method.
 * @author Felix Dobslaw
 *
 */
public class PerformanceAnalyzerPlain extends PerformanceAnalyzer {

	public PerformanceAnalyzerPlain(){

		amountTasks = 10;
		task = new SomeJob(100000000, 4000000, new SomeOption());
		executions = 3;
		poolSize = new int[]{1,2,8,9,10};
	}

	public static void main(String[] args) throws InterruptedException,
			ExecutionException {
		new PerformanceAnalyzerPlain().analyze();
	}
}