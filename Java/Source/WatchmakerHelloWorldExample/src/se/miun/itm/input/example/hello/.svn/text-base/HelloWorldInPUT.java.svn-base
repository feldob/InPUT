package se.miun.itm.input.example.hello;

import java.io.FileNotFoundException;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.impOrt.InPUTArchiveImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * A complete example of how InPUT can be used to execute reproducible,
 * documentable, and easy to change and adjustable experimental series.
 * 
 * @author Felix Dobslaw
 * 
 */
public class HelloWorldInPUT {

	public static void main(String[] args) throws InPUTException,
			FileNotFoundException {

		IInPUT input = InPUT.getInPUT(new InPUTArchiveImporter(
				"StringIdentification", "StringIdentification.inp"));
		IExperiment experiment = input.impOrt("HelloWorld",
				new ExperimentArchiveImporter("HelloWorld.exp"));

		initSeed(experiment);

		EvolutionEngine<String> engine = experiment.getValue("EA");

		int popSize = experiment.getValue("EA.PopSize");
		int elite = experiment.getValue("EA.EliteCount");
		TerminationCondition termination = experiment
				.getValue("EA.Termination");

		addGenerationWiseLoggingSupport(engine);

		engine.evolve(popSize, elite, termination);
	}

	private static void initSeed(IExperiment experiment) throws InPUTException {
		Random rng = experiment.getValue(Q.RANDOM);
		rng.setSeed((Long)experiment.getValue(Q.SEED));
	}

	private static void addGenerationWiseLoggingSupport(
			EvolutionEngine<String> engine) {
		engine.addEvolutionObserver(new EvolutionObserver<String>() {
			public void populationUpdate(PopulationData<? extends String> data) {
				System.out.printf("Generation %d: %s\n",
						data.getGenerationNumber(), data.getBestCandidate());
			}
		});
	}
}
