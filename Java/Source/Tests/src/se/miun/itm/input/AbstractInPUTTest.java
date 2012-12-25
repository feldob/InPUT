package se.miun.itm.input;

import org.junit.After;

import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.model.param.ParamStore;

public abstract class AbstractInPUTTest {

	@After
	public void tearDown() {
		Mappings.releaseAllMappings();
		ParamStore.releaseAllParamStores();
	}
}
