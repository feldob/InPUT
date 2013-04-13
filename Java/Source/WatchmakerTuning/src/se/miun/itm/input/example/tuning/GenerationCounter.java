package se.miun.itm.input.example.tuning;

import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

public class GenerationCounter implements EvolutionObserver<String> {

	private volatile int generationsToSuccessCounter;
	
	public GenerationCounter() {
		generationsToSuccessCounter = 0;
	}
	
	@Override
	public synchronized void populationUpdate(PopulationData<? extends String> data) {
		generationsToSuccessCounter++;
	}

	public synchronized void reset() {
		generationsToSuccessCounter = 0;
	}
	
	public int getGenerationsToSuccess() {
		return generationsToSuccessCounter;
	}
}
