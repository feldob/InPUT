package se.miun.itm.input.tuning;

import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;

public interface ITuner {

	
	/**
	 * retrieve the next set of designs to be analyzed.
	 * @param runs TODO
	 * @return
	 * @throws InPUTException 
	 */
	List<IExperiment> nextIteration() throws InPUTException;
}