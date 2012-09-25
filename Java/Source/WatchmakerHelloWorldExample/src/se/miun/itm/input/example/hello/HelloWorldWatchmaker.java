package se.miun.itm.input.example.hello;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.uncommons.maths.random.MersenneTwisterRNG;
import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.examples.strings.StringEvaluator;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.factories.StringFactory;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;
import org.uncommons.watchmaker.framework.operators.StringCrossover;
import org.uncommons.watchmaker.framework.operators.StringMutation;
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection;
import org.uncommons.watchmaker.framework.termination.TargetFitness;

/**
 * This code has been copied from XXX, the Watchmaker framework, as an example for an initial
 * example of using it to solve optimization problems. It is originally been written by XXX.
 * It is used here as a reference to show the power of InPUT with respect to algorithm/problem
 * configurability.
 */
public class HelloWorldWatchmaker {

	public static void main(String[] args) {

		char[] chars = new char[27];
		for (char c = 'A'; c <= 'Z'; c++)
		{
		    chars[c - 'A'] = c;
		}
		chars[26] = ' ';
		
		CandidateFactory<String> factory = new StringFactory(chars, 11);
		
		List<EvolutionaryOperator<String>> operators = new LinkedList<EvolutionaryOperator<String>>();
		operators.add(new StringMutation(chars, new Probability(0.02)));
		operators.add(new StringCrossover());
		EvolutionaryOperator<String> pipeline = new EvolutionPipeline<String>(
				operators);

		FitnessEvaluator<String> fitnessEvaluator = new StringEvaluator("HELLO WORLD");
		SelectionStrategy<Object> selection = new RouletteWheelSelection();
		Random rng = new MersenneTwisterRNG();

		EvolutionEngine<String> engine = new GenerationalEvolutionEngine<String>(
				factory, pipeline, fitnessEvaluator, selection, rng);

		engine.addEvolutionObserver(new EvolutionObserver<String>()
				{
			public void populationUpdate(PopulationData<? extends String> data)
			{
				System.out.printf("Generation %d: %s\n",
						data.getGenerationNumber(),
						data.getBestCandidate());
			}
				});
		
		String result = engine.evolve(10, 0, new TargetFitness(0,false));
		System.out.println(result);
		
	}
}
