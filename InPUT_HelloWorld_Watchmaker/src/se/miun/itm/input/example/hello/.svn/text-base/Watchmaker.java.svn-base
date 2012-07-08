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

public class Watchmaker {

	private static final String INVESTIGATION_ID = "StringIdentification";

	private static final String INVESTIGATION = INVESTIGATION_ID + ".inp";

	private static final String EXPERIMENT_ID = "HelloWorld";

	private static final String EXPERIMENT = EXPERIMENT_ID + ".exp";

	public static void main(String[] args) throws InPUTException,
			FileNotFoundException {

		IInPUT input = InPUT.getInPUT(new InPUTArchiveImporter(INVESTIGATION_ID,
				INVESTIGATION));
		IExperiment experiment = input.impOrt(EXPERIMENT_ID,
				new ExperimentArchiveImporter(EXPERIMENT));

		initSeed(experiment);
				
		EvolutionEngine<String> engine = experiment.getValue("Algorithm");
		
		int popSize = experiment.getValue("Algorithm.EA.PopSize");
		int elite = experiment.getValue("Algorithm.EA.EliteCount");
		TerminationCondition termination = experiment
				.getValue("Algorithm.EA.Termination");

		addGenerationWiseLoggingSupport(engine);
		
		engine.evolve(popSize, elite, termination);
	}

	private static void initSeed(IExperiment experiment) throws InPUTException {
		long seed = experiment.getValue("Seed");
		Random rng = experiment.getValue(Q.RANDOM);
		rng.setSeed(seed);
	}

	private static void addGenerationWiseLoggingSupport(EvolutionEngine<String> engine) {
		engine.addEvolutionObserver(new EvolutionObserver<String>() {
			public void populationUpdate(PopulationData<? extends String> data) {
				System.out.printf("Generation %d: %s\n",
						data.getGenerationNumber(), data.getBestCandidate());
			}
		});
	}
}
