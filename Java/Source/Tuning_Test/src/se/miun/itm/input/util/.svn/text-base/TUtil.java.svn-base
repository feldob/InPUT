package se.miun.itm.input.util;

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
