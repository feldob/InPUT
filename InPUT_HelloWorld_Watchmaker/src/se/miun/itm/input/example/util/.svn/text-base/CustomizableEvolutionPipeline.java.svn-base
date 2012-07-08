package se.miun.itm.input.example.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;

public class CustomizableEvolutionPipeline<T> implements
		EvolutionaryOperator<T> {

	private final LinkedList<EvolutionaryOperator<T>> pipeline;

	public CustomizableEvolutionPipeline() {
		pipeline = new LinkedList<EvolutionaryOperator<T>>();
	}

	public void addOperator(EvolutionaryOperator<T> operator) {
		pipeline.add(operator);
	}

	public void removeOperator(int position) {
		if (position >= pipeline.size() - 1)
			throw new IllegalArgumentException(
					"The pipeline contains less than " + position
							+ " operators.");

		pipeline.remove(position);
	}

	public int size() {
		return pipeline.size();
	}

	public void clear() {
		pipeline.clear();
	}

	/**
	 * copied from
	 * org.uncommons.watchmaker.framework.operators.EvolutionPipeline
	 * 
	 * @author: Daniel Dyer
	 * 
	 *          Applies each operation in the pipeline in turn to the selection.
	 * 
	 * @param selectedCandidates
	 *            The candidates to subjected to evolution.
	 * @param rng
	 *            A source of randomness used by all stochastic processes in the
	 *            pipeline.
	 * @return A list of evolved candidates.
	 * 
	 */
	@Override
	public List<T> apply(List<T> selectedCandidates, Random rng) {
		List<T> population = selectedCandidates;
		for (EvolutionaryOperator<T> operator : pipeline) {
			population = operator.apply(population, rng);
		}
		return population;
	}
}