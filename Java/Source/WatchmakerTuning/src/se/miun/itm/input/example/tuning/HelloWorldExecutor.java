package se.miun.itm.input.example.tuning;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.TerminationCondition;
import org.uncommons.watchmaker.framework.termination.GenerationCount;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public class HelloWorldExecutor {

	private final GenerationCounter generationalCounter;
	private final Random idGenerator;

	public HelloWorldExecutor() {
		idGenerator = new Random();
		generationalCounter = new GenerationCounter();
	}

	protected IDesign execute(IExperiment experiment) throws InPUTException {

		EvolutionEngine<String> engine = experiment.getValue("EA");
		engine.addEvolutionObserver(generationalCounter);

		int popSize = experiment.getValue("EA.PopSize");
		int elite = experiment.getValue("EA.EliteCount");
		TerminationCondition termination = experiment
				.getValue("EA.Termination");

		engine.evolve(popSize, elite, termination, new GenerationCount(10000));

		IDesign results = prepareResult(experiment,
				generationalCounter.getGenerationsToSuccess());

		generationalCounter.reset();

		return results;

	}

	private IDesign prepareResult(IExperiment experiment, int fitness)
			throws InPUTException {
		IDesign output = experiment.getInPUT().getOutputSpace()
				.nextEmptyDesign("" + idGenerator.nextLong());
		output.setValue("Y", new BigDecimal(fitness));
		return output;
	}

}
