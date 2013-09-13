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