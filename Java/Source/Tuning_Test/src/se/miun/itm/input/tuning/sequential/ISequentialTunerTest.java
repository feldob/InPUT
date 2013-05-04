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

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import model.SomeStructuralParent;
import model.YetAnotherThirdChoice;

import org.junit.Test;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.tuning.ITunerTest;
import se.miun.itm.input.util.TUtil;

public class ISequentialTunerTest extends ITunerTest<SequentialTuner> {

	protected static List<IDesign> initialResults = new ArrayList<IDesign>();

	protected static List<IDesign> sequentialResults = new ArrayList<IDesign>();

	protected static IDesignSpace outputSpace = SequentialTuner.getSingleObjectiveSpace();

	static {
		try {
			IDesign output;
			for (int i = 1; i < 19; i++) {
				output = outputSpace.nextEmptyDesign("" + i);
				output.setValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER, new BigDecimal(i));

				if (i < 13)
					initialResults.add(output);
				sequentialResults.add(output);
			}

		} catch (InPUTException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInitialNextIteration() throws InPUTException {
		IExperiment experiment = tuner.nextExperiment();
		tuner.feedback(initialResults.get(0));

		assertEquals(12, tuner.getCurrentDesignSize());
		assertEquals(0, tuner.getCurrentDesignPointer());

		for (int i = 0; i < 2; i++) {
			assertEquals(experiment, tuner.nextExperiment());
			tuner.feedback(initialResults.get(i + 1));
		}
		assertFalse(experiment.equals(tuner.nextExperiment()));

		TUtil.assertExpectedType(experiment, TUtil.STRUCTURAL_PARENT_PARAM, SomeStructuralParent.class);

		if (YetAnotherThirdChoice.class.isInstance(experiment.getValue(TUtil.STRUCTURAL_PARENT_PARAM))) {
			assertNotNull(experiment.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		} else {
			assertNull(experiment.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		}
	}

	@Test
	public void testSequentialNextIteration() throws InPUTException {
		for (int i = 0; i < initialResults.size(); i++) {
			tuner.nextExperiment();
			tuner.feedback(initialResults.get(i));
		}
		IExperiment seqExperiment = tuner.nextExperiment();

		assertEquals(18, tuner.currentDesignSize());

		TUtil.assertExpectedType(seqExperiment, TUtil.STRUCTURAL_PARENT_PARAM, SomeStructuralParent.class);

		if (YetAnotherThirdChoice.class.isInstance(seqExperiment.getValue(TUtil.STRUCTURAL_PARENT_PARAM))) {
			assertNotNull(seqExperiment.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		} else {
			assertNull(seqExperiment.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		}
	}

	@Test
	public void testFeedback() throws InPUTException {

		IExperiment experiment;
		IDesign result;
		for (int i = 0; i < initialResults.size(); i++) {
			experiment = tuner.nextExperiment();
			result = initialResults.get(i);
			tuner.feedback(result);
			assertEquals(result.getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER),
					experiment.getOutput().get(i%3).getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER));
		}
	}

	@Test
	public void testEvaluations() throws InPUTException {
		assertEquals(0, tuner.getAmountInvestigatedConfigurations());

		for (int i = 0; i < initialResults.size(); i++) {
			tuner.nextExperiment();
			tuner.feedback(initialResults.get(i));
		}

		assertEquals(4, tuner.getAmountInvestigatedConfigurations());
		assertEquals(12, tuner.getAmountEvaluatedRuns());
		
		for (int i = 0; i < sequentialResults.size(); i++) {
			tuner.nextExperiment();
			tuner.feedback(sequentialResults.get(i));
		}
		assertEquals(30, tuner.getAmountEvaluatedRuns());
		assertEquals(10, tuner.getAmountInvestigatedConfigurations());
	}
	//
	// @Test
	// public void testGetBest() throws InPUTException {
	// assertNull(tuner.getBest());
	// tuner.nextIteration();
	// tuner.feedback(initialResults);
	// assertEquals(new BigDecimal("12"), tuner.getBest().getOutput().get(2).getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER));
	// }

}