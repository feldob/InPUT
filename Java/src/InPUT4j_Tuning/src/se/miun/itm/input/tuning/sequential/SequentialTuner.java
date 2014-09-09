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
 */package se.miun.itm.input.tuning.sequential;

import java.util.List;
import java.util.Random;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.Tuner;

// storing all experiments is too expensive! They are anyways exported each time the algorithm is getting into another round!
public abstract class SequentialTuner extends Tuner implements ISequentialTuner {

	public static final String SINGLE_OUTPUT_PARAMETER = "Y";

	private IExperiment currentExperiment;

	private int currentDesignPointer; // points to the position in the current
										// design which is under investigation

	protected int currentDesignSize; // gives the total size of the current
										// design under investigation

	private List<IDesign> problems;

	// TODO adjust for resumable investigations!
	private int amountInvestigatedConfigurations = 0;
	// TODO adjust for resumable investigations!
	private int amountEvaluatedRuns = 0;

	private Random rng = new Random();

	private final boolean randomProblemChoice;

	private int problemPositionCounter = 0;

	/**
	 * A sequential tuner requires an experimental context to be set, that
	 * extends the use of an IInPUT element to a concrete problem instance under
	 * investigation. To differentiate between different problem instances
	 * allows for a differentiated analysis of the results with respect to
	 * problem features. However, the setting of problem is optional; a
	 * <code>problem</code> context might not even be reasonable, desired, or
	 * available (e.g. when interested in configurations for a single possibly
	 * very special instance only). When defined, it has to match the problem
	 * feature space provided by <code>input</code>.
	 * 
	 * @param input
	 * @param studyId
	 * @param problem
	 * @throws InPUTException
	 */
	public SequentialTuner(IInPUT input, List<IDesign> problems, String studyId, boolean minProblem, boolean randomProblemChoice) throws InPUTException {
		super(input, studyId, minProblem);
		this.problems = problems;
		this.randomProblemChoice = randomProblemChoice;
		if (input.getOutputSpace() == null)
			throw new InPUTException("You have to explicitly set an output space. The most basic one is available via the constant \"SINGLE_OBJECTIVE_SPACE\".");
	}

	private IDesign randomInstance() {
		int next = rng.nextInt(problems.size());
		return problems.get(next);
	}

	@Override
	public void resetStudy(List<IDesign> problems, String studyId) throws InPUTException {
		this.problems = problems;
		currentDesignPointer = 0;
		amountInvestigatedConfigurations = 0;
		amountEvaluatedRuns = 0;
		currentDesignSize = getTotalAmountRunsInitialDesign();
	}

	protected abstract void feedback(IExperiment experiment, IDesign newResult) throws InPUTException;

	abstract int initNextDesign() throws InPUTException;

	abstract IExperiment nextExperiment(int position) throws InPUTException;

	@Override
	public int currentDesignSize() {
		return currentDesignSize;
	}

	@Override
	public void feedback(IDesign result) throws InPUTException {
		currentDesignPointer++;
		amountEvaluatedRuns++;
		// add the result as output to the current experiment and serialize to
		// the result file!
		feedback(currentExperiment, result);
	}

	@Override
	/**
	 *  either get the next experiment from the current design or if the current design is done, start a new and take the first one.
	 */
	public IExperiment nextExperiment() throws InPUTException {
		if (currentDesignPointer == 0 || currentDesignPointer == currentDesignSize) {
			currentDesignPointer = 0;
			currentDesignSize = initNextDesign();
		}

		IExperiment nextExperiment = nextExperiment(currentDesignPointer);

		IDesign nextProblem = getNextProblem();
		nextExperiment.setProblemFeatures(nextProblem);

		if (!nextExperiment.same(currentExperiment)) {
			currentExperiment = nextExperiment;
			amountInvestigatedConfigurations++;
		}

		return currentExperiment;
	}

	private IDesign getNextProblem() {
		if (problems == null || problems.size() == 0)
			return null;

		IDesign problem;
		if (randomProblemChoice) {
			problem = randomInstance();
		} else {
			problem = problems.get(problemPositionCounter);
			problemPositionCounter = (problemPositionCounter + 1) % problems.size();
		}
		return problem;
	}

	public IExperiment getExperimentUnderInvestigation() {
		return currentExperiment;
	}

	public int getAmountInvestigatedConfigurations() {
		return amountInvestigatedConfigurations;
	}

	public int getCurrentDesignPointer() {
		return currentDesignPointer > 0 ? currentDesignPointer - 1 : 0;
	}

	public int getAmountEvaluatedRuns() {
		return amountEvaluatedRuns;
	}

	public void emulateNextExperiment(IExperiment experiment) {
		if (currentDesignPointer == currentDesignSize - 1) {
			currentDesignPointer = 0;
			currentDesignSize = emulateNextDesignAndReturnSize();
		}

		if (!experiment.same(currentExperiment)) {
			currentExperiment = experiment;
			amountInvestigatedConfigurations++;
		}

		currentDesignPointer++;
		amountEvaluatedRuns++;
	}

	protected abstract int emulateNextDesignAndReturnSize();

}