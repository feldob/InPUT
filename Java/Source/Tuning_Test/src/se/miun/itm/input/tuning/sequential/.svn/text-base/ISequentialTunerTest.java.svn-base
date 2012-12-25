package se.miun.itm.input.tuning.sequential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	
	protected static IDesignSpace outputSpace = SequentialTuner
			.getSingleObjectiveSpace();
	
	static {
		try {
			IDesign output;
			for (int i = 1; i < 19; i++) {
				output = outputSpace.nextEmptyDesign("" + i);
				output.setValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER,
						new BigDecimal(i));

				if (i<13) 
					initialResults.add(output);
				sequentialResults.add(output);
			}

		} catch (InPUTException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHasNextIteration() throws InPUTException {
		for (int i = 0; i < 3; i++) {
			assertEquals(true, tuner.hasNextIteration());
			tuner.nextIteration();		}
//		assertEquals(false, tuner.hasNextIteration());
//		
//		try {
//			tuner.nextIteration();
//			fail("There is no more iteration, exception expected!");
//		} catch (Exception e) {
//			
//		}
	}

	@Test
	public void testInitialNextIteration() throws InPUTException {
		List<IExperiment> experiments;
			experiments = tuner.nextIteration();

			assertEquals(12, experiments.size());
			assertTrue(experiments.get(11).same(experiments.get(9)));
			assertTrue(!experiments.get(9).same(experiments.get(8)));

			IExperiment design;
			for (int j = 0; j < experiments.size(); j++) {
				design = experiments.get(j);
				TUtil.assertExpectedType(design, TUtil.STRUCTURAL_PARENT_PARAM,
						SomeStructuralParent.class);

				if (YetAnotherThirdChoice.class.isInstance(design
						.getValue(TUtil.STRUCTURAL_PARENT_PARAM))) {
					assertNotNull(design
							.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
				} else {
					assertNull(design
							.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
				}
			}
	}

	@Test
	public void testSequentialNextIteration() throws InPUTException {
		List<IExperiment> designs = tuner.nextIteration();
		tuner.feedback(initialResults);

			designs = tuner.nextIteration();

			assertEquals(18, designs.size());
			assertTrue(designs.get(0).same(designs.get(2)));
			assertFalse(designs.get(2).same(designs.get(3)));

			IExperiment design;
			for (int j = 0; j < designs.size(); j++) {
				design = designs.get(j);
				TUtil.assertExpectedType(design, TUtil.STRUCTURAL_PARENT_PARAM,
						SomeStructuralParent.class);

				if (YetAnotherThirdChoice.class.isInstance(design
						.getValue(TUtil.STRUCTURAL_PARENT_PARAM))) {
					assertNotNull(design
							.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
				} else {
					assertNull(design
							.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
				}
			}
	}

	@Test
	public void testFeedback() throws InPUTException {
		tuner.nextIteration();
		tuner.feedback(initialResults);
		List<IExperiment> experiments = tuner.getResults();

		int j = 0;
		for (IExperiment experiment : experiments) {
			for (IDesign output : experiment.getOutput()) {
				assertEquals(
						initialResults.get(j).getValue(
								SequentialTuner.SINGLE_OUTPUT_PARAMETER),
						output.getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER));
				j++;
			}
		}
	}

	@Test
	public void testEvaluations() throws InPUTException {
		assertEquals(0, tuner.getAmountInvestigatedConfigurations());
		try {
			tuner.getExperiment(4);
			fail("the tuner does not contain such an entry!");
		} catch (Exception e) {
		}
		tuner.nextIteration();
		tuner.feedback(initialResults);
		assertEquals(4, tuner.getAmountInvestigatedConfigurations());
		assertEquals(12, tuner.getAmountEvaluatedRuns());
		tuner.nextIteration();
		assertEquals(4, tuner.getAmountInvestigatedConfigurations());
		assertEquals(12, tuner.getAmountEvaluatedRuns());
		tuner.feedback(sequentialResults);
		assertEquals(30, tuner.getAmountEvaluatedRuns());
		assertEquals(9, tuner.getAmountInvestigatedConfigurations());

		assertTrue(tuner.getAmountEvaluatedRuns()>=tuner.getAmountInvestigatedConfigurations());
	}

	@Test
	public void testGetResults() throws InPUTException {
		tuner.nextIteration();
		tuner.feedback(initialResults);
		List<IExperiment> results = tuner.getResults();

		int expected = 1;
			for (IExperiment experiment : results) {
				for (IDesign output : experiment.getOutput()) {
					assertEquals(
							new BigDecimal(expected),
							output.getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER));
					expected++;
				}
			}
	}

	@Test
	public void testGetBest() throws InPUTException {
		assertNull(tuner.getBest());
		tuner.nextIteration();
		tuner.feedback(initialResults);
		assertEquals(new BigDecimal("12"), tuner.getBest().getOutput().get(2)
				.getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER));
	}

}