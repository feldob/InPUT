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
 */package se.miun.itm.input.model.param;

import java.util.Random;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.NValue;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.generator.FixedNumericGenerator;
import se.miun.itm.input.model.param.generator.NumericGenerator;
import se.miun.itm.input.model.param.generator.RandomNumericGenerator;
import se.miun.itm.input.util.Q;

/**
 * A concrete implementation of a parameter, which represents numerical values,
 * given their definition ranges and their evaluation engine.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class NParam extends Param<NumericGenerator> {

	private static final String NUMERIC_PATTERN = "-?\\d+(\\.\\d+)?";
	private static final long serialVersionUID = 4936437855938927299L;

	public NParam(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
	}

	@Override
	protected NumericGenerator initGenerator(boolean initRanges)
			throws InPUTException {
		String fixedValue = getFixedValue();
		NumericGenerator generator;
		if (isValidFixedValue(fixedValue))
			generator = new FixedNumericGenerator(this, fixedValue);
		else {
			generator = new RandomNumericGenerator(this, initRandom(ps));
		}

		if (initRanges) {
			generator.initRanges();
		}
		return generator;
	}

	private Random initRandom(ParamStore ps) {
		if (ps != null)
			return ps.getRNG();
		return new Random();
	}

	// has to be done after the order is defined, otherwise not semantically
	// correct.
	public void initRanges() throws InPUTException {
		generator.initRanges();
	}

	@Override
	public Class<?> getInPUTClass() {
		if (generator.hasWrapper())
			return generator.getWrapperClass();
		return generator.getPrimitiveClass();
	}

	public void isValid(String paramId, Object value, ElementCache elementCache)
			throws InPUTException {
		if (generator.hasWrapper())
			value = invokeGetter(value);
		generator.validateInPUT(paramId, value, elementCache);
	}

	@Override
	public Object invokeGetter(Object value) throws InPUTException {
		return generator.invokeGetter(value);
	}

	@Override
	public void initValue(Value<?> nValue, Object[] actualParams,
			ElementCache elementCache) throws InPUTException {
		String valueString = nValue.getAttributeValue(Q.VALUE_ATTR);
		// retrieve the numeric of the element
		Object value = generator.parse(valueString);
		if (generator.hasWrapper()) {
			// if wrapper object -> instantiate the wrapper object
			value = generator.newInstance(new Object[] { value });
		}
		nValue.setInputValue(value);
	}

	public void initValueAttribute(NValue nValue, Object value)
			throws InPUTException {
		String valueString;
		// differentiate between wrapper and plain types.
		if (generator.hasWrapper())
			// with a wrapper, the 'real' value has to be retrieved
			valueString = invokeGetter(value).toString();
		else
			// otherwise, simply tostring it, that works for all plain types.
			valueString = value.toString();
		// now, set the value to the element.
		nValue.setAttribute(Q.VALUE_ATTR, valueString);
	}

	public void initNumericElementFromValue(NValue nValue, Object value)
			throws InPUTException {
		if (!value.getClass().isArray())
			nValue.setAttribute(Q.VALUE_ATTR, value.toString());
		else
			nValue.setInputValue(generator.hasWrapper() ? null : value);
	}

	@Override
	public String getParamId() {
		return getId();
	}

	@Override
	public void checkIfParameterSettable(String paramId) throws InPUTException {

	}

	@Override
	public boolean isPlainValueElement(Value<?> valueElement) {
		// / retrieve the value String of the element
		String valueString = valueElement.getAttributeValue(Q.VALUE_ATTR);
		return (isValidFixedValue(valueString) && !valueString.equals(Q.NULL))
				|| !isArrayType()
				|| (isArrayType() && !getId().equals(getId()));
	}

	public String getMaxValue() throws InPUTException {
		return generator.getMaxValue();
	}

	public String getMinValue() throws InPUTException {
		return generator.getMinValue();
	}

	public boolean isCountable() {
		return generator.isCountable();
	}

	public boolean hasWrapper() {
		return generator.hasWrapper();
	}

	@Override
	public Object getValueForString(String valueString) throws InPUTException {
		return generator.parse(valueString);
	}

	@Override
	public String getValueTypeString() {
		return Q.NVALUE;
	}

	@Override
	public String toString() {
		return generator.toString();
	}

	public String getNumericMaxValue() throws InPUTException {
		return generator.getNumericMaxValue();
	}

	public String getNumericMinValue() throws InPUTException {
		return generator.getNumericMinValue();
	}

	public boolean isBoolean() {
		return generator.isBoolean();
	}

	public void setFixed(String value) throws InPUTException {
		if (value == null)
			removeAttribute(Q.FIXED_ATTR);
		else if (isValidFixedValue(value))
			setAttribute(Q.FIXED_ATTR, value);
		else
			throw new InPUTException(
					"the value you entered is not of correct numeric type.");

		generator = initGenerator(true);
	}

	//TODO refactor this into generator class
	private boolean isValidFixedValue(String value) {
		return value != null && (value.matches(NUMERIC_PATTERN) || value.equals("true") || value.equals("false"));
	}

	@Override
	public boolean isFixed() {
		return getAttributeValue(Q.FIXED_ATTR) != null;
	}

	@Override
	public String getFixedValue() {
		return getAttributeValue(Q.FIXED_ATTR);
	}
}