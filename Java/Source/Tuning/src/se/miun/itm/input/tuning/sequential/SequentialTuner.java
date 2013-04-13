package se.miun.itm.input.tuning.sequential;

import java.util.List;
import java.util.Random;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.Tuner;
import se.miun.itm.input.util.Q;

// storing all experiments is too expensive! They are anyways exported each time the algorithm is getting into another round!
public abstract class SequentialTuner extends Tuner implements ISequentialTuner {

	public static final String SINGLE_OUTPUT_PARAMETER = "Y";
	//
	private IExperiment currentExperiment;

	private int currentDesignPointer; // points to the position in the current design which is under investigation

	protected int currentDesignSize; // gives the total size of the current design under investigation

	private List<IDesign> problems;
	//
	// private IDesign bestOutput;

	private final Random rng;
	//
	private int amountInvestigatedConfigurations = 0;

	private int amountEvaluatedRuns = 0;

	/**
	 * A sequential tuner requires an experimental context to be set, that extends the use of an IInPUT element to a concrete problem
	 * instance under investigation. To differentiate between different problem instances allows for a differentiated analysis of the
	 * results with respect to problem features. However, the setting of problem is optional; a <code>problem</code> context might not even
	 * be reasonable, desired, or available (e.g. when interested in configurations for a single possibly very special instance only). When
	 * defined, it has to match the problem feature space provided by <code>input</code>.
	 * 
	 * @param input
	 * @param studyId
	 * @param problem
	 * @throws InPUTException
	 */
	public SequentialTuner(IInPUT input, List<IDesign> problems, String studyId, boolean minProblem) throws InPUTException {
		super(input, studyId);
		this.problems = problems;
		rng = InPUTConfig.getValue(Q.RANDOM);
		if (input.getOutputSpace() == null)
			throw new InPUTException(
					"You have to explicitly set an output space. The most basic one is available via the constant \"SINGLE_OBJECTIVE_SPACE\".");
	}

	private IDesign randomInstance() {
		if (problems == null)
			return null;
		return problems.get(rng.nextInt(problems.size()));
	}

	// @Override
	// public IExperiment getBest() {
	// return best;
	// }

	@Override
	public void resetStudy(List<IDesign> problems, String studyId) throws InPUTException {
		this.problems = problems;
		currentDesignPointer = 0;
		amountInvestigatedConfigurations = 0;
		amountEvaluatedRuns = 0;
		currentDesignSize = getTotalAmountRunsInitialDesign();
	}

	// @Override
	// public final void feedback(List<IDesign> results) throws InPUTException {
	// if (results.size() != experimentalDesignsUnderTest.size())
	// throw new InPUTException("The amount of results is expected to be as long as the amonut of designs under investigation.");

	// create the experiments from the input
	// IExperiment experiment;
	// for (int i = 0; i < results.size(); i++) {
	// experiment = experimentalDesignsUnderTest.get(i);
	// experiment.addOutput(results.get(i));
	// }

	// give the implementing tuner the chance to analyze the results.
	// internalFeedback(experimentalDesignsUnderTest);

	// add the experiments to the ones so far
	// mergeEvaluated();
	//
	// // check which one is the best
	// decideBest();
	//
	// // empty the current cache
	// experimentalDesignsUnderTest.clear();
	// }
	//
	// private void mergeEvaluated() throws InPUTException {
	// for (IExperiment experiment : experimentalDesignsUnderTest) {
	// if (configurationHasAlreadyBeenEvaluated(experiment))
	// mergeExistingExperimentIntoEvaluated(experiment);
	// else
	// evaluated.add(experiment);
	// }
	// }
	//
	// private void mergeExistingExperimentIntoEvaluated(IExperiment experiment) throws InPUTException {
	// IExperiment eval = getEvaluatedConfigurationWithSameConfiguration(experiment);
	// for (IDesign output : experiment.getOutput()) {
	// eval.addOutput(output);
	// }
	//
	// for (String name : experiment.getContentNames()) {
	// eval.addContent(name, experiment.getContentFor(name));
	// }
	// }
	//
	// private void decideBest() throws InPUTException {
	// if (best == null)
	// initBest(evaluated);
	//
	// for (IExperiment experiment : evaluated)
	// for (IDesign output : experiment.getOutput())
	// if (isBetter(output))
	// resetBest(experiment, output);
	//
	// }
	//
	// public boolean isBetter(IDesign output) throws InPUTException {
	// int compValue = ((BigDecimal) output.getValue(SINGLE_OUTPUT_PARAMETER)).compareTo((BigDecimal) bestOutput
	// .getValue(SINGLE_OUTPUT_PARAMETER));
	//
	// if (minProblem) {
	// return compValue < 0;
	// }else{
	// return compValue > 0;
	// }
	// }
	//
	// private void initBest(List<IExperiment> experiments) {
	// IExperiment experiment = experiments.get(0);
	// resetBest(experiment, experiment.getOutput().get(0));
	// }
	//
	// private void resetBest(IExperiment experiment, IDesign output) {
	// best = experiment;
	// bestOutput = output;
	// }

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
		// add the result as output to the current experiment and serialize to the result file!
		feedback(currentExperiment, result);
	}

	@Override
	/**
	 *  either get the next experiment from the current design or if the current design is done, start a new and take the first one.
	 */
	public IExperiment nextExperiment() throws InPUTException {
		if (currentDesignPointer== 0 || currentDesignPointer == currentDesignSize) {
			currentDesignPointer = 0;
			currentDesignSize = initNextDesign();
		}
		IExperiment nextExperiment = nextExperiment(currentDesignPointer);
		nextExperiment.setProblemFeatures(randomInstance());

		if (!nextExperiment.same(currentExperiment)) {
			currentExperiment = nextExperiment;
			amountInvestigatedConfigurations++;
		}
		
		return currentExperiment;
	}

	public IExperiment getExperimentUnderInvestigation() {
		return currentExperiment;
	}

	public int getAmountInvestigatedConfigurations() {
		return amountInvestigatedConfigurations;
	}

	public int getCurrentDesignPointer() {
		return currentDesignPointer > 0 ? currentDesignPointer-1 : 0;
	}

	public int getCurrentDesignSize() {
		return currentDesignSize;
	}

	public int getAmountEvaluatedRuns() {
		return amountEvaluatedRuns;
	}

}