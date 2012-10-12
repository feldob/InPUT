package se.miun.itm.util;

import java.util.Random;

import org.junit.Before;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public class InPUTTest {

	protected IDesignSpace space;

	protected Random rng;

	@Before
	public void setUp() throws Exception {
		space = new DesignSpace("testSpace.xml");
		rng = InPUTConfig.getValue("random");
		rng.setSeed(123456789);
	}
}
