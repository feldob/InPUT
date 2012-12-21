package se.miun.itm.input.model.param.generator;

import java.util.Map;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.param.NParam;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public class FixedNumericGenerator extends NumericGenerator {

	private final String fixedValue;

	private Object value;

	public FixedNumericGenerator(NParam param, String fixedValue) throws InPUTException {
		super(param, null);
		this.fixedValue = fixedValue;
	}

	private void init() {
		value = parse(fixedValue);
	}

	@Override
	public Object next(Map<String, Object> vars) throws InPUTException {
		if (value == null)
			init();
		return value;
	}

	@Override
	public void validateInPUT(Object value, ElementCache elementCache) throws InPUTException {
		super.validateInPUT(value, elementCache);
		if (!value.toString().equals(fixedValue))
			throw new InPUTException(param.getId()+": you have entered the value \"" + value.toString() + "\" that is not allowed by this fixed parameter. Only \"" + fixedValue + "\" is allowed.");
	}
}