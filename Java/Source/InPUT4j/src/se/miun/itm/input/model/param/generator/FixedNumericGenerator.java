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
 */package se.miun.itm.input.model.param.generator;

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

	public FixedNumericGenerator(NParam param, String fixedValue) throws InPUTException {
		super(param, null);
		this.fixedValue = fixedValue;
	}

	@Override
	public Object next(Map<String, Object> vars) throws InPUTException {
		return parse(fixedValue);
	}

	@Override
	public void validateInPUT(String paramId, Object value, ElementCache elementCache) throws InPUTException {
		super.validateInPUT(paramId, value, elementCache);
		if (!value.toString().equals(fixedValue))
			throw new InPUTException(param.getId()+": you have entered the value \"" + value.toString() + "\" that is not allowed by this fixed parameter. Only \"" + fixedValue + "\" is allowed.");
	}
}