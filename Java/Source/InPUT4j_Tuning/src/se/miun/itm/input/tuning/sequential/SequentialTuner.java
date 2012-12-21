package se.miun.itm.input.tuning.sequential;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.Tuner;
import se.miun.itm.input.util.Q;

public abstract class SequentialTuner extends Tuner implements ISequentialTuner {

	public static final String SINGLE_OUTPUT_PARAMETER = "Y";

	private IExperiment best;

	private final List<IExperiment> experimentalDesignsUnderTest = new LinkedList<IExperiment>();

	private final List<IExperiment> evaluated = new LinkedList<IExperiment>();

	private List<IDesign> problems;

	private IDesign bestOutput;

	private final Random rng;

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
	public SequentialTuner(IInPUT input, List<IDesign> problems, String studyId) throws InPUTException {
		super(input, studyId);
		this.problems = problems;
		rng = InPUTConfig.getValue(Q.RANDOM);
		if (input.getOutputSpace() == null)
			throw new InPUTException(
					"You have to explicitly set an output space. The most basic one is available via the constant \"SINGLE_OBJECTIVE_SPACE\".");
	}

	@Override
	public final List<IExperiment> nextIteration() throws InPUTException {
		List<IExperiment> designs = nextInternalIteration();
		for (IExperiment experiment : designs) {
			experiment.setProblemFeatures(randomInstance());
		}
		experimentalDesignsUnderTest.clear();
		// TODO integrate into old here! possible doubles.
		experimentalDesignsUnderTest.addAll(designs);
		return designs;
	}

	private IDesign randomInstance() {
		if (problems == null)
			return null;
		return problems.get(rng.nextInt(problems.size()));
	}

	protected abstract List<IExperiment> nextInternalIteration() throws InPUTException;

	@Override
	public int getAmountInvestigatedConfigurations() {
		return evaluated.size();
	}

	@Override
	public int getAmountEvaluatedRuns() {
		int counter = 0;
		for (IExperiment experiment : evaluated)
			counter += experiment.getOutput().size();
		return counter;
	}

	@Override
	public List<IExperiment> getResults() {
		return new ArrayList<IExperiment>(evaluated);
	}

	@Override
	public IExperiment getExperiment(int position) {
		return evaluated.get(position - 1);
	}

	@Override
	public IExperiment getBest() {
		return best;
	}

	@Override
	public void resetStudy(List<IDesign> problems, String studyId) throws InPUTException {
		this.problems = problems;
		best = null;
		experimentalDesignsUnderTest.clear();
		evaluated.clear();
	}

	@Override
	public final void feedback(List<IDesign> results) throws InPUTException {
		if (results.size() != experimentalDesignsUnderTest.size())
			throw new InPUTException("The amount of results is expected to be as long as the amonut of designs under investigation.");

		// create the experiments from the input
		IExperiment experiment;
		for (int i = 0; i < results.size(); i++) {
			experiment = experimentalDesignsUnderTest.get(i);
			experiment.addOutput(results.get(i));
			experiment.setProblemFeatures(randomInstance());
		}

		// give the implementing tuner the chance to analyze the results.
		internalFeedback(experimentalDesignsUnderTest);

		// add the experiments to the ones so far
		mergeEvaluated();

		// check which one is the best
		decideBest();

		// empty the current cache
		experimentalDesignsUnderTest.clear();
	}

	private void mergeEvaluated() throws InPUTException {
		for (IExperiment experiment : experimentalDesignsUnderTest) {
			if (configurationHasAlreadyBeenEvaluated(experiment))
				mergeExistingExperimentIntoEvaluated(experiment);
			else
				evaluated.add(experiment);
		}
	}

	private void mergeExistingExperimentIntoEvaluated(IExperiment experiment) throws InPUTException {
		IExperiment eval = getEvaluatedConfigurationWithSameConfiguration(experiment);
		for (IDesign output : experiment.getOutput()) {
			eval.addOutput(output);
		}

		for (String name : experiment.getContentNames()) {
			eval.addContent(name, experiment.getContentFor(name));
		}
	}

	private IExperiment getEvaluatedConfigurationWithSameConfiguration(IExperiment experiment) {
		for (IExperiment eval : evaluated)
			if (eval.investigatesSameConfiguration(experiment))
				return eval;
		return null;
	}

	private boolean configurationHasAlreadyBeenEvaluated(IExperiment experiment) {
		for (IExperiment eval : evaluated)
			if (eval.investigatesSameConfiguration(experiment))
				return true;
		return false;
	}

	private void decideBest() throws InPUTException {
		if (best == null)
			initBest(evaluated);

		for (IExperiment experiment : evaluated)
			for (IDesign output : experiment.getOutput())
				if (((BigDecimal) output.getValue(SINGLE_OUTPUT_PARAMETER)).compareTo((BigDecimal) bestOutput
						.getValue(SINGLE_OUTPUT_PARAMETER)) > 0)
					resetBest(experiment, output);

	}

	private void initBest(List<IExperiment> experiments) {
		IExperiment experiment = experiments.get(0);
		resetBest(experiment, experiment.getOutput().get(0));
	}

	private void resetBest(IExperiment experiment, IDesign output) {
		best = experiment;
		bestOutput = output;
	}

	protected abstract void internalFeedback(List<IExperiment> results) throws InPUTException;

	@Override
	public List<IExperiment> getEvaluatedShallow() {
		return evaluated;
	}
}