package se.miun.itm.input.tuning.sequential;

import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.ITuner;

public interface ISequentialTuner extends ITuner {

	/**
	 * Does the current experimental design have more experimental configurations to be tested?
	 * @return
	 */
	boolean hasNextIteration();

	/**
	 * after receiving the experimental design, the results have to be fed back to SPOT.
	 * @param results
	 * @throws InPUTException 
	 */
	void feedback(List<IDesign> results) throws InPUTException;
	/**
	 * receive the best experimental setup together with its experimental context. 
	 * @return
	 */
	IExperiment getBest();
	
	/**
	 * receive the experiment that contains the experimental run in <code>position</code>.
	 * @param number
	 */
	IExperiment getExperiment(int position);
	
	/**
	 * receive all experiments and their results.
	 * @return
	 */
	List<IExperiment> getResults();
	
	/**
	 * returns the complete amount of experimental runs
	 * @return
	 */
	int getAmountEvaluatedRuns();
	
	/**
	 * returns the amount of tested parameter combinations. Each combination is represented by an <code>IExperiment</code> object.
	 * @return
	 */
	int getAmountInvestigatedConfigurations();
	
	/**
	 * resets the tuner
	 * @param studyId TODO
	 * @throws InPUTException 
	 */
	void resetStudy(List<IDesign> problems, String studyId) throws InPUTException;

	List<IExperiment> getEvaluatedShallow();
}