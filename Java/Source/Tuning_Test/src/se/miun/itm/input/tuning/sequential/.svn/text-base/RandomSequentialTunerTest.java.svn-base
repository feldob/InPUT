package se.miun.itm.input.tuning.sequential;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;

import model.SomeStructuralParent;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.TUtil;

public class RandomSequentialTunerTest extends ISequentialTunerTest {

	@Before
	public void setUp() throws InPUTException {
		super.setUp();
		tuner = new RandomizedSequentialTuner(input,null, null, 12, 10);
	}

	@Test
	public void testInternalFeedback() {
	}

	@Test
	public void testEvaluations() throws InPUTException {
		assertEquals(0, tuner.getAmountInvestigatedConfigurations());
		try {
			tuner.getExperiment(12);
			fail("the tuner does not contain such an entry!");
		} catch (Exception e) {
		}
		tuner.nextIteration();
		tuner.feedback(initialResults);
		assertEquals(12, tuner.getAmountInvestigatedConfigurations());
		assertEquals(12, tuner.getAmountEvaluatedRuns());
		tuner.nextIteration();
		assertEquals(12, tuner.getAmountInvestigatedConfigurations());
		assertEquals(12, tuner.getAmountEvaluatedRuns());
		tuner.feedback(initialResults);
		assertEquals(24, tuner.getAmountEvaluatedRuns());
		assertEquals(24, tuner.getAmountInvestigatedConfigurations());

		assertTrue(tuner.getAmountEvaluatedRuns()>=tuner.getAmountInvestigatedConfigurations());
	}
	

	@Test
	public void testSequentialNextIteration() throws InPUTException {
		assertTrue("same as initial design, always.", true);
	}

	@Test
	public void testGetBest() throws InPUTException {
		assertNull(tuner.getBest());
		tuner.nextIteration();
		tuner.feedback(initialResults);
		assertEquals(new BigDecimal("12"), tuner.getBest().getOutput().get(0)
				.getValue(SequentialTuner.SINGLE_OUTPUT_PARAMETER));
	}

	
	@Test
	public void testInitialNextIteration() throws InPUTException {
		List<IExperiment> designs;
		for (int i = 0; i < 3; i++) {
			designs = tuner.nextIteration();
			assertEquals(12, designs.size());

			TUtil.assertExpectedType(designs.get(0),
					TUtil.STRUCTURAL_PARENT_PARAM, SomeStructuralParent.class);
			TUtil.assertExpectedType(designs.get(1),
					TUtil.STRUCTURAL_PARENT_PARAM, SomeStructuralParent.class);
			TUtil.assertExpectedType(designs.get(2),
					TUtil.STRUCTURAL_PARENT_PARAM, SomeStructuralParent.class);
			TUtil.assertExpectedType(designs.get(3),
					TUtil.STRUCTURAL_PARENT_PARAM, SomeStructuralParent.class);
		}
	}
}