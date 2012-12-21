package se.miun.itm.input.tuning.sequential;

import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public class RandomizedSequentialTuner extends SequentialTuner {

	private final int totalIterations;
	
	private int currentIterations;
	
	private final int size;

	public RandomizedSequentialTuner(IInPUT input, List<IDesign> problems, String studyId, int size, int iterations) throws InPUTException {
		super(input, problems, studyId);
		totalIterations = iterations;
		this.size = size;
		currentIterations = 0;
	}

	@Override
	public boolean hasNextIteration() {
		return currentIterations < totalIterations;
	}
	
	@Override
	protected List<IExperiment> nextInternalIteration()
			throws InPUTException {
		if (currentIterations > totalIterations)
			throw new InPUTException("There are no more designs available to be tested from this tuner.");
		currentIterations++;
		List<IExperiment> designs = new ArrayList<IExperiment>();
		IExperiment experiment;
		for (int i = 1; i <= size; i++) {
			experiment = new Experiment(""+i, input);
			experiment.setAlgorithmDesign(input.getAlgorithmDesignSpace().nextDesign("" + i));
			designs.add(experiment);
		}
		return designs;
	}

	@Override
	protected void internalFeedback(List<IExperiment> results) {
		// TODO Auto-generated method stub

	}

}
