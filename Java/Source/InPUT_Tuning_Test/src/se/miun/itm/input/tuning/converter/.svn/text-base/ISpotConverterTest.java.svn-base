package se.miun.itm.input.tuning.converter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.TUtil;

public class ISpotConverterTest<ExternalDesign, ExternalSpace, ExternalResult> {

	protected InputConverter<ExternalDesign, ExternalSpace, ExternalResult> converter;

	protected static IDesignSpace spaceNumeric, spaceStructural,
			spaceCombined;

	protected static List<IDesign> designsNumeric, designsStructural,
			designsCombined;

	protected static IInPUT numeric, structural, combined;

	static {
		try {
			spaceNumeric = new DesignSpace("numericalSpace.xml");
			spaceStructural = new DesignSpace("structuralSpace.xml");
			spaceCombined = new DesignSpace("combinedSpace.xml");

			designsNumeric = initDesigns(spaceNumeric);
			designsStructural = initDesigns(spaceStructural);
			designsCombined = initDesigns(spaceStructural);

			numeric = new InPUT(TUtil.NUMERIC_SPACE_ID, spaceNumeric, null,
					null, null);
			structural = new InPUT(TUtil.STRUCTURAL_SPACE_ID, spaceStructural,
					null, null, null);
			combined = new InPUT(TUtil.COMBINED_SPACE_ID, spaceCombined, null,
					null, null);
		} catch (InPUTException e) {
			e.printStackTrace();
		}
	}

	private static List<IDesign> initDesigns(IDesignSpace space)
			throws InPUTException {
		List<IDesign> designs = new ArrayList<IDesign>();
		for (int i = 0; i < 5; i++)
			designs.add(space.nextDesign("design" + i));
		return designs;
	}

	@Test(expected = InPUTException.class)
	public void testDesignToNegative() throws InPUTException {
		converter.toExperiments(null, null);
	}

	@Test(expected = InPUTException.class)
	public void testSpaceToNegative() throws InPUTException {
		converter.toDesignSpace(null);
	}

	@Test(expected = InPUTException.class)
	public void testDesignFromNegative() throws InPUTException {
		List<IExperiment> designs = new ArrayList<IExperiment>();
		Object designsExternal = converter.fromExperiments(designs);
		assertNull(designsExternal);
		converter.fromExperiments(null);
	}

	@Test(expected = InPUTException.class)
	public void testSpaceFromNegative() throws InPUTException {
		converter.fromExperiments(null);
	}

	@Test
	public void testSpaceTo() {
		fail("Not yet implemented: Probably no need to be implemented either.");
	}

}