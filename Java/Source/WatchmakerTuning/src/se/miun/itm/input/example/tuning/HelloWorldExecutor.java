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
 */package se.miun.itm.input.example.tuning;

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
