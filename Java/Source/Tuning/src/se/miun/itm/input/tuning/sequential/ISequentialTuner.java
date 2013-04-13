package se.miun.itm.input.tuning.sequential;

import java.util.List;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.ITuner;

public interface ISequentialTuner extends ITuner {

//	/**
//	 * receive the best experimental setup together with its experimental context. 
//	 * @return
//	 */
//	IExperiment getBest();
//	
//	/**
//	 * returns the complete amount of experimental runs
//	 * @return
//	 */
//	int getAmountEvaluatedRuns();
//	
//	/**
//	 * returns the amount of tested parameter combinations. Each combination is represented by an <code>IExperiment</code> object.
//	 * @return
//	 */
//	int getAmountInvestigatedConfigurations();
//	
	/**
	 * resets the tuner
	 * @param studyId TODO
	 * @throws InPUTException 
	 */
	void resetStudy(List<IDesign> problems, String studyId) throws InPUTException;

	int currentDesignSize();
	
	/**
	 * after each experiment, the results have to be fed back to SPOT.
	 * @param result
	 * @throws InPUTException
	 */
	void feedback(IDesign result) throws InPUTException;

	int getTotalAmountRunsInitialDesign() throws InPUTException;
}