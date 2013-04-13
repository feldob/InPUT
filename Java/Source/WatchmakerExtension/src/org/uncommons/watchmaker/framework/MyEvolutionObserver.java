package org.uncommons.watchmaker.framework;

public interface MyEvolutionObserver<T> {
	
	 /**
     * Invoked when the state of the population has changed (typically
     * at the end of a generation).
     * @param data Statistics about the state of the current generation.
     */
    void populationUpdate(MyPopulationData<T> data);
}