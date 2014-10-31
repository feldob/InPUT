/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package org.uncommons.watchmaker.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MyGenerationalEvolutionEngine<T> extends MyAbstractEvolutionEngine<T> {

	private final EvolutionaryOperator<T> evolutionScheme;
	private final FitnessEvaluator<? super T> fitnessEvaluator;
	private final SelectionStrategy<? super T> selectionStrategy;

	/**
	 * Creates a new evolution engine by specifying the various components
	 * required by a generational evolutionary algorithm.
	 * 
	 * @param candidateFactory
	 *            Factory used to create the initial population that is
	 *            iteratively evolved.
	 * @param evolutionScheme
	 *            The combination of evolutionary operators used to evolve the
	 *            population at each generation.
	 * @param fitnessEvaluator
	 *            A function for assigning fitness scores to candidate
	 *            solutions.
	 * @param selectionStrategy
	 *            A strategy for selecting which candidates survive to be
	 *            evolved.
	 * @param rng
	 *            The source of randomness used by all stochastic processes
	 *            (including evolutionary operators and selection strategies).
	 */
	// public MyGenerationalEvolutionEngine(CandidateFactory<T>
	// candidateFactory, EvolutionaryOperator<T> evolutionScheme,
	// FitnessEvaluator<? super T> fitnessEvaluator, SelectionStrategy<? super
	// T> selectionStrategy, int popSize, int eliteCount,
	// TerminationCondition termination, Random rng) {
	// super(candidateFactory, fitnessEvaluator, popSize, eliteCount,
	// termination, rng);
	// this.evolutionScheme = evolutionScheme;
	// this.fitnessEvaluator = fitnessEvaluator;
	// this.selectionStrategy = selectionStrategy;
	// }

	public MyGenerationalEvolutionEngine(CandidateFactory<T> candidateFactory, EvolutionaryOperator<T> evolutionScheme,
			FitnessEvaluator<? super T> fitnessEvaluator, SelectionStrategy<? super T> selectionStrategy, int popSize, int eliteCount,
			TerminationCondition[] termination, Random rng) {
		super(candidateFactory, fitnessEvaluator, popSize, eliteCount, termination, rng);
		this.evolutionScheme = evolutionScheme;
		this.fitnessEvaluator = fitnessEvaluator;
		this.selectionStrategy = selectionStrategy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation, int eliteCount, Random rng) {
		List<T> population = new ArrayList<T>(evaluatedPopulation.size());

		// First perform any elitist selection.
		List<T> elite = new ArrayList<T>(eliteCount);
		Iterator<EvaluatedCandidate<T>> iterator = evaluatedPopulation.iterator();
		while (elite.size() < eliteCount) {
			elite.add(iterator.next().getCandidate());
		}
		// Then select candidates that will be operated on to create the evolved
		// portion of the next generation.
		population.addAll(selectionStrategy.select(evaluatedPopulation, fitnessEvaluator.isNatural(), evaluatedPopulation.size() - eliteCount, rng));
		// Then evolve the population.
		population = evolutionScheme.apply(population, rng);
		// When the evolution is finished, add the elite to the population.
		population.addAll(elite);
		return evaluatePopulation(population);
	}

	@Override
	public void addEvolutionObserver(EvolutionObserver<? super T> observer) {
		throw new IllegalArgumentException("not supported");
	}

	@Override
	public void removeEvolutionObserver(EvolutionObserver<? super T> observer) {
		throw new IllegalArgumentException("not supported");
	}
}
