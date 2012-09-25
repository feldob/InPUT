/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */
package se.miun.itm.input.model.param;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import org.jdom2.Element;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.eval.EvaluationEngine;
import se.miun.itm.input.eval.InputEvaluator;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.model.Ranges;
import se.miun.itm.input.util.Q;

/**
 * A concrete implementation of a parameter, which represents numerical values, given their definition ranges and their evaluation engine. 
 * @author Felix Dobslaw
 *
 */
public class NParam extends Param {

	private static final long serialVersionUID = 4936437855938927299L;

	private final Numeric type;

	// @LazyInit
	private Ranges ranges;

	// @LazyLoading
	private InputEvaluator eval;

	public NParam(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		type = Numeric.valueOf(getAttributeValue(Q.TYPE_ATTR).split(
				Q.ESCAPED_ARRAY_START)[0].toUpperCase());
	}

	// has to be done after the order is defined, otherwise not semantically
	// correct.
	public void initRanges() throws InPUTException {
		ranges = new Ranges(this);
	}

	@Override
	public Method initGetMethod(Object parentValue) throws InPUTException {
		String getter = getGetter();
		Method handle;
		if (mapping.hasWrapper())
			try {
				handle = mapping.getWrapperClass().getMethod(getter,
						AStruct.EMPTY_CLASS_ARRAY);
			} catch (NoSuchMethodException e) {
				throw new InPUTException(getId()
						+ ": There is no getter method by name '" + getter
						+ "'.", e);
			} catch (SecurityException e) {
				throw new InPUTException(getId() + ": access to the method '"
						+ getter
						+ " ' is not allowed due to security restrictions.", e);
			}
		else
			try {
				handle = parentValue.getClass().getMethod(getter,
						AStruct.EMPTY_CLASS_ARRAY);
			} catch (NoSuchMethodException e) {
				throw new InPUTException(getId()
						+ ": There is no getter method by name '" + getter
						+ "'.", e);
			} catch (SecurityException e) {
				throw new InPUTException(getId() + ": access to the method '"
						+ getter
						+ " ' is not allowed due to security restrictions.", e);
			}
		return handle;
	}

	@Override
	protected Method initSetMethod(Object parentValue) throws InPUTException {
		String setter;
		Class<?> cLass;
		if (mapping.hasWrapper()) {
			cLass = mapping.getWrapperClass();
			setter = getWrapperSetter();
		} else {
			cLass = getNumericType().getNumClass();
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
						getNumericType().getPrimitiveClass());
			} catch (NoSuchMethodException e2) {
				throw new InPUTException(getId()
						+ ": There is no setter method by name '" + setter
						+ "'.", e2);
			} catch (SecurityException e2) {
				throw new InPUTException(getId() + ": access to the method '"
						+ setter
						+ " ' is not allowed due to security restrictions.", e2);
			}
		}
		return m;
	}

	public Numeric getNumericType() {
		return type;
	}

	public Class<?> getWrapperClass() {
		return mapping.getWrapperClass();
	}

	public String getWrapperGetter() {
		return mapping.getWrapperGetter();
	}

	public String getWrapperSetter() {
		return mapping.getWrapperSetter();
	}

	public boolean hasWrapper() {
		return mapping.hasWrapper();
	}

	public Constructor<?> getWrapperConstructor() throws InPUTException {

		return mapping.getWrapperConstructor(type);
	}

	public Ranges getEvaluatedRanges(Map<String, Object> vars)
			throws InPUTException {
		Ranges ranges;
		if (isIndependant())
			ranges = this.ranges;
		else {
			if (eval == null)
				eval = InPUTConfig.getValue("evaluator");
			ranges = EvaluationEngine.evaluate(eval, this.ranges, vars);
		}
		return ranges;
	}

	@Override
	public String getParamId() {
		return getId();
	}

	@Override
	public Object getFixedValue() {
		return type.parse(getFixed());
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

	@Override
	public Class<?> getInPUTClass() {
		if (hasWrapper())
			return getWrapperClass();
		return getNumericType().getPrimitiveClass();
	}
	
	@Override
	public boolean isComplex() {
		return false;
	}
}