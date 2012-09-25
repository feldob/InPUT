package se.miun.itm.input.example.concurrent;

import se.miun.itm.input.annotation.Get;
import se.miun.itm.input.annotation.Input;

public class PerformanceAnalyzer {

	@Get(value = "runtime", from = "performance")
	private Object[] runtimes;
	
	@Input(id = "performance", file = "performance.xml")
	public static void main(String[] args) {
		new PerformanceAnalyzer();
	}
	
	public PerformanceAnalyzer() {
		long sum;
		Object[] runtime;
		for (int i = 0; i < runtimes.length; i++) {
			sum = 0;
			runtime = (Object[])runtimes[i];
			for (int j = 0; j < runtime.length; j++) {
				sum += (Long)runtime[j];
			}
			System.out.println("Average performance for setup nr. "+ (i+1) +" is:" + (double)sum/runtime.length);
		}
	}
}