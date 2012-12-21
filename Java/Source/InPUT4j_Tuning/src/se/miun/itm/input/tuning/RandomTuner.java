package se.miun.itm.input.tuning;

import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public class RandomTuner extends Tuner {

	private int size;

	public RandomTuner(IInPUT input,IDesign problem, String studyId, int size) throws InPUTException {
		super(input, studyId);
		this.size = size;
	}

	@Override
	public List<IExperiment> nextIteration() throws InPUTException {
		List<IExperiment> designs = new ArrayList<IExperiment>();
		for (int i = 1; i <= size; i++) {
			IExperiment experiment = new Experiment(""+i, input);
			experiment.setAlgorithmDesign(input.getAlgorithmDesignSpace().nextDesign("" + i));
			designs.add(experiment);
		}
		return designs;
	}
}