package org.uncommons.watchmaker.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.interactive.InteractiveSelection;

public class MyGenerationalEvolutionEngine<T> extends MyAbstractEvolutionEngine<T> {

	  private final EvolutionaryOperator<T> evolutionScheme;
	    private final FitnessEvaluator<? super T> fitnessEvaluator;
	    private final SelectionStrategy<? super T> selectionStrategy;

	    /**
	     * Creates a new evolution engine by specifying the various components required by
	     * a generational evolutionary algorithm.
	     * @param candidateFactory Factory used to create the initial population that is
	     * iteratively evolved.
	     * @param evolutionScheme The combination of evolutionary operators used to evolve
	     * the population at each generation.
	     * @param fitnessEvaluator A function for assigning fitness scores to candidate
	     * solutions.
	     * @param selectionStrategy A strategy for selecting which candidates survive to
	     * be evolved.
	     * @param rng The source of randomness used by all stochastic processes (including
	     * evolutionary operators and selection strategies).
	     */
	    public MyGenerationalEvolutionEngine(CandidateFactory<T> candidateFactory,
	                                       EvolutionaryOperator<T> evolutionScheme,
	                                       FitnessEvaluator<? super T> fitnessEvaluator,
	                                       SelectionStrategy<? super T> selectionStrategy,
	                                       Random rng)
	    {
	        super(candidateFactory, fitnessEvaluator, rng);
	        this.evolutionScheme = evolutionScheme;
	        this.fitnessEvaluator = fitnessEvaluator;
	        this.selectionStrategy = selectionStrategy;
	    }


	    /**
	     * Creates a new evolution engine for an interactive evolutionary algorithm.  It
	     * is not necessary to specify a fitness evaluator for interactive evolution.
	     * @param candidateFactory Factory used to create the initial population that is
	     * iteratively evolved.
	     * @param evolutionScheme The combination of evolutionary operators used to evolve
	     * the population at each generation.
	     * @param selectionStrategy Interactive selection strategy configured with appropriate
	     * console.
	     * @param rng The source of randomness used by all stochastic processes (including
	     * evolutionary operators and selection strategies).
	     */
	    public MyGenerationalEvolutionEngine(CandidateFactory<T> candidateFactory,
	                                       EvolutionaryOperator<T> evolutionScheme,
	                                       InteractiveSelection<T> selectionStrategy,
	                                       Random rng)
	    {
	        this(candidateFactory,
	             evolutionScheme,
	             new NullFitnessEvaluator(), // No fitness evaluations to perform.
	             selectionStrategy,
	             rng);
	    }


	    /**
	     * {@inheritDoc} 
	     */
	    @Override
	    protected List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation,
	                                                            int eliteCount,
	                                                            Random rng)
	    {
	        List<T> population = new ArrayList<T>(evaluatedPopulation.size());

	        // First perform any elitist selection.
	        List<T> elite = new ArrayList<T>(eliteCount);
	        Iterator<EvaluatedCandidate<T>> iterator = evaluatedPopulation.iterator();
	        while (elite.size() < eliteCount)
	        {
	            elite.add(iterator.next().getCandidate());
	        }
	        // Then select candidates that will be operated on to create the evolved
	        // portion of the next generation.
	        population.addAll(selectionStrategy.select(evaluatedPopulation,
	                                                   fitnessEvaluator.isNatural(),
	                                                   evaluatedPopulation.size() - eliteCount,
	                                                   rng));
	        // Then evolve the population.
	        population = evolutionScheme.apply(population, rng);
	        // When the evolution is finished, add the elite to the population.
	        population.addAll(elite);
	        return evaluatePopulation(population);
	    }
}
