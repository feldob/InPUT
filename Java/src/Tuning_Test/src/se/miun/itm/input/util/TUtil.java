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
 */package se.miun.itm.input.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.tuning.sequential.spot.SpotDES;
import se.miun.itm.input.tuning.sequential.spot.SpotROI;

public class TUtil {

	public static final String NON_SHARED_PRIMITIVE_SUB_PARAM = "SomeStructuralParent.YetAnotherThirdChoice.SomeChoiceSpecificPrimitiveSub";
	public static final String STRUCTURAL_PARENT_PARAM = "SomeStructuralParent";
	public static final String SHARED_PRIMITIVE_PARAM = "SomeStructuralParent.SomeSharedPrimitiveSub";
	public static final String SOME_STRUCTURAL_PARAM = "SomeStructural";
	public static final String SIMPLE_INT_PARAM = "SimpleInt";
	public static final String SOME_FIXED_STRUCTURAL_ARRAY = "SomeFixedSingleArrayStructural";
	
	public static final String NUMERIC_SPACE_ID = "numeric";
	public static final String STRUCTURAL_SPACE_ID = "structural";
	public static final String COMBINED_SPACE_ID = "combined";

	public static SpotROI ROI_NUMERIC;
	public static SpotROI ROI_STRUCTURAL;
	public static SpotROI ROI_COMBINED;

	public static SpotDES DES_NUMERIC;
	public static SpotDES DES_STRUCTURAL;
	public static SpotDES DES_COMBINED;

	static{
		try {
			ROI_NUMERIC = new SpotROI("numeric.roi", 123, "numeric");
		ROI_STRUCTURAL = new SpotROI("structural.roi",123,  "structural");
		ROI_COMBINED = new SpotROI("combined.roi",123,  "combined");
		
		DES_NUMERIC = new SpotDES("numeric.des", ROI_NUMERIC);
		DES_STRUCTURAL = new SpotDES("structural.des", ROI_STRUCTURAL);
		DES_COMBINED = new SpotDES("combined.des", ROI_COMBINED);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void assertExpectedParameter(IExperiment design, String paramId,
			Object expected) throws InPUTException {
		Object is = design.getValue(paramId);
		assertEquals(expected, is);
	}

	public static void assertExpectedType(IExperiment design, String paramId,
			Class<?> expected) throws InPUTException {
		Object is = design.getValue(paramId);
		assertTrue(expected.isInstance(is));
	}
	
	public static void main(String[] args) {
		System.out.println(new BigDecimal("true"));
	}

	
}
