package se.miun.itm.input.model.param.generator;

import java.util.Map;
import java.util.Random;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.NParam;

/**
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class RandomNumericGenerator extends NumericGenerator {

	public RandomNumericGenerator(NParam param, Random rng)
			throws InPUTException {
		super(param, rng);
	}

	@Override
	public Object next(Map<String, Object> vars) throws InPUTException {
		return ranges.next(rng, vars);
	}
}