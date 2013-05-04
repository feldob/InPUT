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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.EvolutionUtils;
import org.uncommons.watchmaker.framework.FitnessEvaluationWorker;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.TerminationCondition;

public abstract class MyAbstractEvolutionEngine<T> implements EvolutionEngine<T> {
	 // A single multi-threaded worker is shared among multiple evolution engine instances.
    private static FitnessEvaluationWorker concurrentWorker = null;

    private final Set<EvolutionObserver<? super T>> observers = new CopyOnWriteArraySet<EvolutionObserver<? super T>>();

    private final Set<MyEvolutionObserver<T>> myObservers = new CopyOnWriteArraySet<MyEvolutionObserver<T>>();
    
    private final Random rng;
    private final CandidateFactory<T> candidateFactory;
    private final FitnessEvaluator<? super T> fitnessEvaluator;

    private volatile boolean singleThreaded = false;

    private List<TerminationCondition> satisfiedTerminationConditions;


    /**
     * Creates a new evolution engine by specifying the various components required by
     * an evolutionary algorithm.
     * @param candidateFactory Factory used to create the initial population that is
     * iteratively evolved.
     * @param fitnessEvaluator A function for assigning fitness scores to candidate
     * solutions.
     * @param rng The source of randomness used by all stochastic processes (including
     * evolutionary operators and selection strategies).
     */
    protected MyAbstractEvolutionEngine(CandidateFactory<T> candidateFactory,
                                      FitnessEvaluator<? super T> fitnessEvaluator,
                                      Random rng)
    {
        this.candidateFactory = candidateFactory;
        this.fitnessEvaluator = fitnessEvaluator;
        this.rng = rng;
    }


    /**
     * {@inheritDoc}
     */
    public T evolve(int populationSize,
                    int eliteCount,
                    TerminationCondition... conditions)
    {
        return evolve(populationSize,
                      eliteCount,
                      Collections.<T>emptySet(),
                      conditions);
    }


    /**
     * {@inheritDoc}
     */
    public T evolve(int populationSize,
                    int eliteCount,
                    Collection<T> seedCandidates,
                    TerminationCondition... conditions)
    {
        return evolvePopulation(populationSize,
                                eliteCount,
                                seedCandidates,
                                conditions).get(0).getCandidate();
    }


    /**
     * {@inheritDoc}
     */
    public List<EvaluatedCandidate<T>> evolvePopulation(int populationSize,
                                                        int eliteCount,
                                                        TerminationCondition... conditions)
    {
        return evolvePopulation(populationSize,
                                eliteCount,
                                Collections.<T>emptySet(),
                                conditions);
    }


    /**
     * {@inheritDoc}
     */
    public List<EvaluatedCandidate<T>> evolvePopulation(int populationSize,
                                                        int eliteCount,
                                                        Collection<T> seedCandidates,
                                                        TerminationCondition... conditions)
    {
        if (eliteCount < 0 || eliteCount >= populationSize)
        {
            throw new IllegalArgumentException("Elite count must be non-negative and less than population size.");
        }
        if (conditions.length == 0)
        {
            throw new IllegalArgumentException("At least one TerminationCondition must be specified.");
        }

        satisfiedTerminationConditions = null;
        int currentGenerationIndex = 0;
        long startTime = System.currentTimeMillis();

        List<T> population = candidateFactory.generateInitialPopulation(populationSize,
                                                                        seedCandidates,
                                                                        rng);

        // Calculate the fitness scores for each member of the initial population.
        List<EvaluatedCandidate<T>> evaluatedPopulation = evaluatePopulation(population);
        EvolutionUtils.sortEvaluatedPopulation(evaluatedPopulation, fitnessEvaluator.isNatural());
        PopulationData<T> data = EvolutionUtils.getPopulationData(evaluatedPopulation,
                                                                  fitnessEvaluator.isNatural(),
                                                                  eliteCount,
                                                                  currentGenerationIndex,
                                                                  startTime);
        // Notify observers of the state of the population.
        notifyPopulationChange(data);

        List<TerminationCondition> satisfiedConditions = EvolutionUtils.shouldContinue(data, conditions);
        while (satisfiedConditions == null)
        {
            ++currentGenerationIndex;
            evaluatedPopulation = nextEvolutionStep(evaluatedPopulation, eliteCount, rng);
            EvolutionUtils.sortEvaluatedPopulation(evaluatedPopulation, fitnessEvaluator.isNatural());
            data = EvolutionUtils.getPopulationData(evaluatedPopulation,
                                                    fitnessEvaluator.isNatural(),
                                                    eliteCount,
                                                    currentGenerationIndex,
                                                    startTime);
            // Notify observers of the state of the population.
            notifyPopulationChange(data);
            notifyMyPopulationChange(new MyPopulationData<T>(data, evaluatedPopulation));
            satisfiedConditions = EvolutionUtils.shouldContinue(data, conditions);
        }
        this.satisfiedTerminationConditions = satisfiedConditions;
        return evaluatedPopulation;
    }


	/**
     * This method performs a single step/iteration of the evolutionary process.
     * @param evaluatedPopulation The population at the beginning of the process.
     * @param eliteCount The number of the fittest individuals that must be preserved.
     * @param rng A source of randomness.
     * @return The updated population after the evolutionary process has proceeded
     * by one step/iteration.
     */
    protected abstract List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation,
                                                                     int eliteCount,
                                                                     Random rng);


    /**
     * Takes a population, assigns a fitness score to each member and returns
     * the members with their scores attached, sorted in descending order of
     * fitness (descending order of fitness score for natural scores, ascending
     * order of scores for non-natural scores).
     * @param population The population to evaluate (each candidate is assigned
     * a fitness score).
     * @return The evaluated population (a list of candidates with attached fitness
     * scores).
     */
    protected List<EvaluatedCandidate<T>> evaluatePopulation(List<T> population)
    {
        List<EvaluatedCandidate<T>> evaluatedPopulation = new ArrayList<EvaluatedCandidate<T>>(population.size());

        if (singleThreaded) // Do fitness evaluations on the request thread.
        {
            for (T candidate : population)
            {
                evaluatedPopulation.add(new EvaluatedCandidate<T>(candidate,
                                                                  fitnessEvaluator.getFitness(candidate, population)));
            }
        }
        else
        {
            // Divide the required number of fitness evaluations equally among the
            // available processors and coordinate the threads so that we do not
            // proceed until all threads have finished processing.
            try
            {
                List<T> unmodifiablePopulation = Collections.unmodifiableList(population);
                List<Future<EvaluatedCandidate<T>>> results = new ArrayList<Future<EvaluatedCandidate<T>>>(population.size());
                // Submit tasks for execution and wait until all threads have finished fitness evaluations.
                for (T candidate : population)
                {
                    results.add(getSharedWorker().submit(new FitnessEvalutationTask<T>(fitnessEvaluator,
                                                                                       candidate,
                                                                                       unmodifiablePopulation)));
                }
                for (Future<EvaluatedCandidate<T>> result : results)
                {
                    evaluatedPopulation.add(result.get());
                }
            }
            catch (ExecutionException ex)
            {
                throw new IllegalStateException("Fitness evaluation task execution failed.", ex);
            }
            catch (InterruptedException ex)
            {
                // Restore the interrupted status, allows methods further up the call-stack
                // to abort processing if appropriate.
                Thread.currentThread().interrupt();
            }
        }

        return evaluatedPopulation;
    }



    /**
     * <p>Returns a list of all {@link TerminationCondition}s that are satisfied by the current
     * state of the evolution engine.  Usually this list will contain only one item, but it
     * is possible that mutliple termination conditions will become satisfied at the same
     * time.  In this case the condition objects in the list will be in the same order that
     * they were specified when passed to the engine.</p>
     *
     * <p>If the evolution has not yet terminated (either because it is still in progress or
     * because it hasn't even been started) then an IllegalStateException will be thrown.</p>
     *
     * <p>If the evolution terminated because the request thread was interrupted before any
     * termination conditions were satisfied then this method will return an empty list.</p>
     *
     * @throws IllegalStateException If this method is invoked on an evolution engine before
     * evolution is started or while it is still in progress.
     *
     * @return A list of statisfied conditions.  The list is guaranteed to be non-null.  The
     * list may be empty because it is possible for evolution to terminate without any conditions
     * being matched.  The only situation in which this occurs is when the request thread is
     * interrupted.
     */
    public List<TerminationCondition> getSatisfiedTerminationConditions()
    {
        if (satisfiedTerminationConditions == null)
        {
            throw new IllegalStateException("EvolutionEngine has not terminated.");
        }
        else
        {
            return Collections.unmodifiableList(satisfiedTerminationConditions);
        }
    }

    /**
     * Adds a listener to receive status updates on the evolution progress.
     * Updates are dispatched synchronously on the request thread.  Observers should
     * complete their processing and return in a timely manner to avoid holding up
     * the evolution.
     * @param observer An evolution observer call-back.
     * @see #removeEvolutionObserver(EvolutionObserver)
     */
    public void addMyEvolutionObserver(MyEvolutionObserver<T> observer)
    {
        myObservers.add(observer);
    }

    /**
     * Adds a listener to receive status updates on the evolution progress.
     * Updates are dispatched synchronously on the request thread.  Observers should
     * complete their processing and return in a timely manner to avoid holding up
     * the evolution.
     * @param observer An evolution observer call-back.
     * @see #removeEvolutionObserver(EvolutionObserver)
     */
    public void addEvolutionObserver(EvolutionObserver<? super T> observer)
    {
        observers.add(observer);
    }


    /**
     * Removes an evolution progress listener.
     * @param observer An evolution observer call-back.
     * @see #addEvolutionObserver(EvolutionObserver)
     */
    public void removeEvolutionObserver(EvolutionObserver<? super T> observer)
    {
        observers.remove(observer);
    }

    /**
     * Removes an evolution progress listener.
     * @param observer An evolution observer call-back.
     * @see #addEvolutionObserver(EvolutionObserver)
     */
    public void removeMyEvolutionObserver(MyEvolutionObserver<? super T> observer)
    {
        myObservers.remove(observer);
    }
    
    /**
     * Send the population data to all registered observers.
     * @param data Information about the current state of the population.
     */
    private void notifyMyPopulationChange(MyPopulationData<T> data)
    {
        for (MyEvolutionObserver<T> observer : myObservers)
        {
            observer.populationUpdate(data);
        }
    }
    
    /**
     * Send the population data to all registered observers.
     * @param data Information about the current state of the population.
     */
    private void notifyPopulationChange(PopulationData<T> data)
    {
        for (EvolutionObserver<? super T> observer : observers)
        {
            observer.populationUpdate(data);
        }
    }


    /**
     * By default, fitness evaluations are performed on separate threads (as many as there are
     * available cores/processors).  Use this method to force evaluation to occur synchronously
     * on the request thread.  This is useful in restricted environments where programs are not
     * permitted to start or control threads.  It might also lead to better performance for
     * programs that have extremely lightweight/trivial fitness evaluations.
     * @param singleThreaded If true, fitness evaluations will be performed synchronously on the
     * request thread.  If false, fitness evaluations will be performed by worker threads.
     */
    public void setSingleThreaded(boolean singleThreaded)
    {
        this.singleThreaded = singleThreaded;
    }


    /**
     * Lazily create the multi-threaded worker for fitness evaluations.
     */
    private static synchronized FitnessEvaluationWorker getSharedWorker()
    {
        if (concurrentWorker == null)
        {
            concurrentWorker = new FitnessEvaluationWorker();
        }
        return concurrentWorker;
    }
}
