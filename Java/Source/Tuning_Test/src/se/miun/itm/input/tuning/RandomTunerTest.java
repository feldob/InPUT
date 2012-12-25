package se.miun.itm.input.tuning;

import org.junit.Before;

import se.miun.itm.input.model.InPUTException;

public class RandomTunerTest extends ITunerTest<RandomTuner> {

	@Before
	public void setUp() throws InPUTException {
		super.setUp();
		tuner = new RandomTuner(input, null, null, 12);
	}
}