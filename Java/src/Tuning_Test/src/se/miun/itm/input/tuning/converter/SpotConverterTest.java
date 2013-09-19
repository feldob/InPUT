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
 */package se.miun.itm.input.tuning.converter;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.List;

import model.SomeAbstractComplexStructural;
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
		List<IExperiment> experiments = testTo(TUtil.DES_COMBINED,
				TUtil.COMBINED_SPACE_ID);

		for (int i = 0; i < experiments.size(); i++)
			TUtil.assertExpectedParameter(experiments.get(i),
					TUtil.SHARED_PRIMITIVE_PARAM, 3);

		TUtil.assertExpectedType(experiments.get(0), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherFirstChoice.class);
		TUtil.assertExpectedType(experiments.get(1), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherSecondChoice.class);
		TUtil.assertExpectedType(experiments.get(2), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherThirdChoice.class);
		TUtil.assertExpectedType(experiments.get(3), TUtil.STRUCTURAL_PARENT_PARAM,
				YetAnotherThirdChoice.class);

		assertNull(experiments.get(0)
				.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		assertNull(experiments.get(1)
				.getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM));
		assertEquals(
				experiments.get(2).getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM),
				0.24f);
		assertEquals(
				experiments.get(3).getValue(TUtil.NON_SHARED_PRIMITIVE_SUB_PARAM),
				0.2f);
		
		Object theFixedArray = experiments.get(0).getValue(TUtil.SOME_FIXED_STRUCTURAL_ARRAY);
		assertNotNull(theFixedArray);
		assertTrue(theFixedArray.getClass().isArray());
		assertEquals(1, Array.getLength(theFixedArray));
		assertTrue(Array.get(theFixedArray, 0) instanceof SomeAbstractComplexStructural);
	}

	private List<IExperiment> testTo(SpotDES des, String spaceId)
			throws InPUTException {

		List<IExperiment> designs = null;

		designs = converter.toExperiments(spaceId, des);
		assertEquals(designs.size(), 4);
		
		return designs;
	}
}