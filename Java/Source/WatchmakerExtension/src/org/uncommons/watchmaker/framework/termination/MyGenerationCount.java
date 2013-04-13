package org.uncommons.watchmaker.framework.termination;

public class MyGenerationCount extends GenerationCount {

	private final int amount;
	
	public MyGenerationCount(int amount) {
		super(amount);
		this.amount = amount;
	}
	
	public int getAmount() {
		return amount;
	}
}