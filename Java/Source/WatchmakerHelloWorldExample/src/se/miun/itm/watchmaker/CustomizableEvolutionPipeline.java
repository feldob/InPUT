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
 */package se.miun.itm.watchmaker;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;

/**
 * An extension of the EvolutionPipe from the Watchmaker framework, which makes it 
 * a more flexible datastructure, that can be used within InPUT in order to support declarative
 * removal or extension of evolutionary operators.
 * @author Felix Dobslaw
 *
 * @param <T>
 */
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