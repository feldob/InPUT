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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.model.Ranges;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.mapping.NumericMapping;
import se.miun.itm.input.model.param.NParam;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public abstract class NumericGenerator extends ValueGenerator<NumericMapping, NParam> {
	
	private static final String BOOLEAN_0 = "0";
	private static final String BOOLEAN_1 = "1";
	protected Ranges ranges;

	public NumericGenerator(NParam param, Random rng) throws InPUTException {
		super(param, rng);
	}

	public NumericGenerator initRanges() throws InPUTException {
		ranges = new Ranges(param);
		return this;
	}

	@Override
	public Object parse(String valueString) {
		return ranges.parse(valueString);
	}

	public Constructor<?> getWrapperConstructor() throws InPUTException {
		return mapping.getWrapperConstructor(ranges.getType());
	}

	public Class<?> getPrimitiveClass() {
		return ranges.getType().getPrimitiveClass();
	}

	@Override
	protected NumericMapping init(Element element) throws InPUTException {
		return new NumericMapping(element);
	}

	public boolean hasWrapper() {
		return mapping.hasWrapper();
	}

	public Class<?> getWrapperClass() {
		return mapping.getWrapperClass();
	}

	public Object newInstance(Object[] params) throws InPUTException {
		try {
			return mapping.getWrapperConstructor(ranges.getType()).newInstance(
					params);
		} catch (InstantiationException e) {
			throw new InPUTException(
					param.getId()
							+ ": The object could not be instantiated due to some reason.",
					e);
		} catch (IllegalAccessException e) {
			throw new InPUTException(param.getId()
					+ ": The constructor you declared is not visible.", e);
		} catch (IllegalArgumentException e) {
			throw new InPUTException(param.getId()
					+ ": There is no such constructor.", e);
		} catch (InvocationTargetException e) {
			throw new InPUTException(
					param.getId()
							+ ": Something went wrong with the creation of the constructor.",
					e);
		}

	}

	public boolean isCountable() {
		return ranges.isCountable();
	}

	//TODO only returns the single first min, therefore is a simplification.
	public String getMinValue() throws InPUTException {
		if (ranges.getStrongTypedMin() == null)
			throw new InPUTException("Parameter '"+ param.getId() + "' of design space '" +param.getSpaceId() + "' has no explicit minimum value set.");
		
		return ranges.getStrongTypedMin()[0].toString();
	}

	//TODO only returns the single first min, therefore is a simplification.
	public String getMaxValue() throws InPUTException {
		if (ranges.getStrongTypedMax() == null)
			throw new InPUTException("Parameter '"+ param.getId() + "' of design space '" +param.getSpaceId() + "' has no explicit maximum value set.");
		
		return ranges.getStrongTypedMax()[0].toString();
	}

	@Override
	protected Method initSetMethod(Object parentValue) throws InPUTException {
		String setter;
		Class<?> cLass;
		if (hasWrapper()) {
			cLass = getWrapperClass();
			setter = mapping.getWrapperSetter();
		} else {
			cLass = ranges.getType().getNumClass();
			setter = getSetter();
		}

		return initMethodHandle(parentValue, setter, cLass);
	}

	private Method initMethodHandle(Object parentValue, String setter,
			Class<?> cLass) throws InPUTException {
		Method m;
		try {
			m = parentValue.getClass().getMethod(setter, cLass);
		} catch (Exception e) {
			try {
				m = parentValue.getClass().getMethod(setter,
						ranges.getType().getPrimitiveClass());
			} catch (NoSuchMethodException e2) {
				throw new InPUTException(param.getId()
						+ ": There is no setter method by name '" + setter
						+ "'.", e2);
			} catch (SecurityException e2) {
				throw new InPUTException(param.getId()
						+ ": access to the method '" + setter
						+ " ' is not allowed due to security restrictions.", e2);
			}
		}
		return m;
	}

	@Override
	public Method initGetMethod(Object parentValue) throws InPUTException {
		String getter = getGetter();
		Method handle;
		if (hasWrapper())
			try {
				handle = getWrapperClass().getMethod(getter,
						ValueGenerator.EMPTY_CLASS_ARRAY);
			} catch (NoSuchMethodException e) {
				throw new InPUTException(param.getId()
						+ ": There is no getter method by name '" + getter
						+ "'.", e);
			} catch (SecurityException e) {
				throw new InPUTException(param.getId()
						+ ": access to the method '" + getter
						+ " ' is not allowed due to security restrictions.", e);
			}
		else
			try {
				handle = parentValue.getClass().getMethod(getter,
						ValueGenerator.EMPTY_CLASS_ARRAY);
			} catch (NoSuchMethodException e) {
				throw new InPUTException(param.getId()
						+ ": There is no getter method by name '" + getter
						+ "'.", e);
			} catch (SecurityException e) {
				throw new InPUTException(param.getId()
						+ ": access to the method '" + getter
						+ " ' is not allowed due to security restrictions.", e);
			}
		return handle;
	}

	@Override
	public void validateInPUT(String paramId, Object value, ElementCache elementCache) throws InPUTException {
		if (hasWrapper())
			mapping.getWrapper().checkTypeSafety(value);
		else
		{
			ranges.checkTypeSafety(value);
			ranges.checkValidity(paramId, value, elementCache);
		}
	}

	@Override
	public boolean initByConstructor(String paramId) {
		return false;
	}
	
	@Override
	public String toString() {
		return ranges.toString();
	}

	public String getNumericMaxValue() throws InPUTException {
		if (ranges.getType() == Numeric.BOOLEAN)
			return BOOLEAN_1;
		return getMaxValue();
	}
	
	public String getNumericMinValue() throws InPUTException {
		if (ranges.getType() == Numeric.BOOLEAN)
			return BOOLEAN_0;
		return getMinValue();
	}

	public boolean isBoolean() {
		return ranges.getType() == Numeric.BOOLEAN;
	}
}