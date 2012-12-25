package se.miun.itm.input.tuning.converter;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;

import model.SomeFirstChoice;
import model.SomeSecondChoice;
import model.SomeThirdChoice;
import model.YetAnotherFirstChoice;
import model.YetAnotherSecondChoice;
import model.YetAnotherThirdChoice;

import org.junit.Before;
import org.junit.Test;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.tuning.sequential.spot.SpotDES;
import se.miun.itm.input.tuning.sequential.spot.SpotRES;
import se.miun.itm.input.tuning.sequential.spot.SpotROI;
import se.miun.itm.input.util.TUtil;

public class SpotConverterTest extends
		ISpotConverterTest<SpotDES, SpotROI, SpotRES> {

	private static final String SIMPLE_FLOAT_PARAM = "SimpleFloat";

	@Before
	public void setMoreUp() throws Exception {
		converter = new SpotConverter();
	}

	@Test
	public void testSpaceFromNumeric() throws FileNotFoundException,
			InPUTException {
		testFrom(TUtil.ROI_NUMERIC, spaceNumeric);
	}

	@Test
	public void testSpaceFromStructural() throws FileNotFoundException,
			InPUTException {
		testFrom(TUtil.ROI_STRUCTURAL, spaceStructural);
	}

	@Test
	public void testSpaceFromCombined() throws FileNotFoundException,
			InPUTException {
		testFrom(TUtil.ROI_COMBINED, spaceCombined);
	}

	private void testFrom(SpotROI roiFile, IDesignSpace space)
			throws FileNotFoundException, InPUTException {
		SpotROI externalDesignSpace = new SpotROI(space);
		assertNotNull(externalDesignSpace);
		assertEqualRoi(roiFile, externalDesignSpace);
	}

	private void assertEqualRoi(SpotROI expectedDesign, SpotROI isDesign)
			throws FileNotFoundException {
		assertNotNull(isDesign);
		assertEquals(expectedDesign, isDesign);
	}

	@Test
	public void testDesignToNumeric() throws InPUTException {
		List<IExperiment> designs = testTo(TUtil.DES_NUMERIC,
				TUtil.NUMERIC_SPACE_ID);

		TUtil.assertExpectedParameter(designs.get(0), SIMPLE_FLOAT_PARAM,
				new BigDecimal("2.0101889994953759"));
		TUtil.assertExpectedParameter(designs.get(1), SIMPLE_FLOAT_PARAM,
				new BigDecimal("3.0103420820088591"));
		TUtil.assertExpectedParameter(designs.get(2), SIMPLE_FLOAT_PARAM,
				new BigDecimal("4.0107341045773355"));
		TUtil.assertExpectedParameter(designs.get(3), SIMPLE_FLOAT_PARAM,
				new BigDecimal("5.010909980296623"));
		TUtil.assertExpectedParameter(designs.get(0), TUtil.SIMPLE_INT_PARAM,
				30);
		TUtil.assertExpectedParameter(designs.get(1), TUtil.SIMPLE_INT_PARAM,
				40);
		TUtil.assertExpectedParameter(designs.get(2), TUtil.SIMPLE_INT_PARAM,
				new Integer(50));
		TUtil.assertExpectedParameter(designs.get(3), TUtil.SIMPLE_INT_PARAM,
				10);
	}

	@Test
	public void testDesignToStructural() throws InPUTException {
		List<IExperiment> designs = testTo(TUtil.DES_STRUCTURAL,
				TUtil.STRUCTURAL_SPACE_ID);

		TUtil.assertExpectedType(designs.get(0), TUtil.SOME_STRUCTURAL_PARAM,
				SomeFirstChoice.class);
		TUtil.assertExpectedType(designs.get(1), TUtil.SOME_STRUCTURAL_PARAM,
				SomeSecondChoice.class);
		TUtil.assertExpectedType(designs.get(2), TUtil.SOME_STRUCTURAL_PARAM,
				SomeThirdChoice.class);
		TUtil.assertExpectedType(designs.get(3), TUtil.SOME_STRUCTURAL_PARAM,
				SomeThirdChoice.class);
	}

	@Test
	public void testDesignFromCombined() throws InPUTException {
		List<IExperiment> designs = testTo(TUtil.DES_COMBINED,
				TUtil.COMBINED_SPACE_ID);

		for (int i = 0; i < designs.size(); i++) {
			TUtil.assertExpectedParameter(designs.get(i),
					TUtil.SHARED_PRIMITIVE_PARAM, 3);
		}

		TUtil.assertExpectedType(designs.get(0), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherFirstChoice.class);
		TUtil.assertExpectedType(designs.get(1), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherSecondChoice.class);
		TUtil.assertExpectedType(designs.get(2), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherThirdChoice.class);
		TUtil.assertExpectedType(designs.get(3), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherThirdChoice.class);

		assertNull(designs.get(0)
				.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		assertNull(designs.get(1)
				.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		assertEquals(
				designs.get(2).getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM),
				0.24f);
		assertEquals(
				designs.get(3).getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM),
				0.2f);
	}

	private List<IExperiment> testTo(SpotDES des, String spaceId)
			throws InPUTException {

		List<IExperiment> designs = null;

		designs = converter.toExperiments(spaceId, des);
		assertEquals(designs.size(), 4);
		
		return designs;
	}
}