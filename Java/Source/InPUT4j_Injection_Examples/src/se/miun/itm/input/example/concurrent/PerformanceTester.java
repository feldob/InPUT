package se.miun.itm.input.example.concurrent;

import java.util.Calendar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import se.miun.itm.input.annotation.Get;
import se.miun.itm.input.annotation.Input;
import se.miun.itm.input.annotation.Output;
import se.miun.itm.input.annotation.Set;

public class PerformanceTester {
	
	@Get(value = "jobs", from = "setup")
	private int amountJobs;

	@Get(value = "tests", from = "setup")
	private int tests;

	@Get(value = "amountThreads", from = "setup")
	private Object[] amountThreads;

	private Object[][] runtime = new Object[amountThreads.length][tests];

	private Executor executor;

	@Get(value = "task", from = "setup")
	private Runnable task;

	@Input(id = "setup", file = "setup.xml")
	@Output(id = "performance", file = "performance.xml", spaceFile = "performanceSpace.xml")
	public static void main(String[] args) {
		new PerformanceTester();
	}

	@Set(value = "runtime", of = "performance", to = "runtime")
	public PerformanceTester() {
		long start;
		for (int k = 0; k < amountThreads.length; k++) {
			for (int i = 0; i < tests; i++) {
				start = now();
				System.out.println("round "+ (k+1) + ", test" + (i+1) + ".");
				executor = Executors
						.newFixedThreadPool((Integer) amountThreads[k]);

				for (int j = 0; j < amountJobs; j++) {
					executor.execute(task);
				}
				runtime[k][i] = now() - start;
			}
		}
	}

	private long now() {
		return Calendar.getInstance().getTimeInMillis();
	}
}