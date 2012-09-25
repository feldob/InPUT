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

package se.miun.itm.input.model.element;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jdom2.Content;
import org.jdom2.Element;

import se.miun.itm.input.model.Dimensions;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.mapping.Complex;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.util.Q;

/**
 * Values are generic containers for parameter values, that are abstractly
 * defined by a private "value" fields of type object. Each value is defined
 * with respect to one specific parameter. That parameter restricts the range of
 * the value.
 * 
 * A value element is responsible to collect the necessary context, based on
 * parameter, element cache etc. in order to instantiate semantically correct
 * instances of the parameter type, considering all user defined meta
 * information. This is done using reflection.
 * 
 * A value can not only be of simple value type, but as well of array or
 * customizable collection type, which makes it very flexible for use. Both,
 * structural and numerical values can be of arbitrary array sizes.
 * 
 * The type can be specified using the xml structures.
 * 
 * @author Felix Dobslaw
 * 
 * @param <AParam>
 * @ThreadSafe
 */
public abstract class Value<AParam extends Param> extends InPUTElement {

	private static final long serialVersionUID = -8840060757041893991L;

	private Object value;

	protected final AParam param;

	protected final Integer[] dimensions;

	protected final ElementCache elementCache;

	public Value(String name, AParam param, Element original,
			Integer[] sizeArray, ElementCache elementCache)
			throws InPUTException {
		super(name, Q.DESIGN_NAMESPACE, param, original);
		dimensions = initDimensions(sizeArray);
		this.param = param;

		String localParamId = param.getAttributeValue(Q.ID_ATTR);
		setAttribute(Q.ID_ATTR, localParamId);
		this.elementCache = elementCache;
	}

	public Value(String name, AParam param, Integer[] sizeArray,
			ElementCache elementCache) throws InPUTException {
		this(name, param, null, sizeArray, elementCache);
	}

	@Override
	public Element addContent(Content subValueE) {
		renewLocalId(subValueE);
		return super.addContent(subValueE);
	}

	@SuppressWarnings("unchecked")
	private void renewLocalId(Content subValueE) {
		if (subValueE instanceof Value<?>) {
			String newFullId;
			Value<? extends Param> sub = (Value<? extends Param>) subValueE;
			String subLocalId = sub.getAttributeValue(Q.ID_ATTR);
			Element subParamParent = sub.getParam().getParentElement();
			if (subParamParent instanceof Param
					&& param.getId().equals(getId())
					&& !subParamParent.equals(param)) {
				newFullId = getId() + "." + getAttributeValue(Q.VALUE_ATTR)
						+ "." + subLocalId;
			} else
				newFullId = getId() + "." + subLocalId;
			sub.setFullId(newFullId);
		}
	}

	private Integer[] initDimensions(Integer[] sizeArray) {
		if (isArrayType(sizeArray))
			return sizeArray;
		else
			return Dimensions.DEFAULT_DIM;
	}

	protected String deriveValueFromArrayType() {
		String type = param.getAttributeValue(Q.TYPE_ATTR);
		if (type == null)
			return null;

		// retrieve amount of entries to delete
		StringBuilder b = new StringBuilder(
				type.split(Q.ESCAPED_ARRAY_START)[0]);
		for (int i = 1; i < dimensions.length; i++) {
			b.append("[");
			if (dimensions[i] > 0)
				b.append(dimensions[i]);
			b.append("]");
		}
		return b.toString();
	}

	/**
	 * uses lazy loading.
	 * 
	 * @param actualParams
	 * @return
	 * @throws InPUTException
	 */
	public Object getInputValue(Object[] actualParams) throws InPUTException {
		if (value == null)
			init(actualParams);
		return value;
	}

	public Integer[] getCurrentDimensions() {
		Integer[] dims = this.dimensions.clone();
		if (dims[0] == 0)
			dims[0] = getChildren().size();
		return dims;
	}

	public boolean isArrayType() {
		return isArrayType(dimensions);
	}

	public String valueToString() {
		return getAttributeValue(Q.VALUE_ATTR);
	}

	public void setInputValue(Object value) throws InPUTException {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[" + super.toString() + ", value = " + valueToString() + "]";
	}

	private void initArray(Object[] actualParams) throws InPUTException {
		// make the return value an array of appropriate size
		Integer[] dim = getCurrentDimensions();
		Object[] value = new Object[dim[0]];
		// container for the lower dimensional value entries
		Value<?> subE;
		// for all children run the casting again.
		for (int i = 0; i < getChildren().size(); i++) {
			subE = (Value<?>) getChildren().get(i);
			// get object for numeric element and set as child
			value[i] = subE.getInputValue(actualParams);
		}
		this.value = value;
	}

	private boolean isArrayType(Integer[] dimensions) {
		return dimensions != null && dimensions.length > 0
				&& (dimensions[0] == 0 || dimensions[0] > 1);
	}

	public Param getParam() {
		return param;
	}

	protected void injectOnParent(Object parentValue) throws InPUTException {
		getInputValue(null);
		param.invokeSetter(parentValue, value);
	}

	protected void init(Object[] actualParams) throws InPUTException {
		// retrieve the value String of the element

		if (getAttributeValue(Q.VALUE_ATTR) != null)
			initValue(actualParams);
		else if (param.isComplex())
			initComplex(actualParams);
		else if (!param.isImplicit())
			initArray(actualParams);
		// customizable input has to be triggered last
		else if (getAttribute(Q.VALUE_ATTR) == null)
			initValue(actualParams);
	}

	@SuppressWarnings("unchecked")
	private void initComplex(Object[] actualParams) throws InPUTException {
		Complex complex = param.getComplex();
		Object complexValue = complex.newInstance();
		List<SValue> children = (List<SValue>)(List<?>)getChildren();
		for (SValue sValue : children) {
			complex.invokeAdd(complexValue, sValue.getInputValue(actualParams));
		}
		value = complexValue;
	}

	protected abstract void initRandom(Map<String, Object> vars,
			Object[] actualParams, boolean lazy) throws InPUTException;

	protected abstract void initValue(Object[] actualParams)
			throws InPUTException;

	public String getLocalId() {
		return getAttributeValue(Q.ID_ATTR);
	}

	protected Object getInputValue() {
		return value;
	}

	public Object random(Integer[] dimensions, Map<String, Object> vars)
			throws InPUTException {
		Object value;
		if (dimensions.length > 1)
			value = randomArray(dimensions, vars);
		else
			value = random(dimensions[0], vars);
		return value;

	}

	private Object random(Integer size, Map<String, Object> vars)
			throws InPUTException {
		return randomValue(size, vars);
	}

	private Object randomValue(Integer size, Map<String, Object> vars)
			throws InPUTException {
		Object value;
		// either only a single value of the type
		if (size == 1) {
			value = randomValue(vars);
		} else {
			// or an array of that type
			Object[] values = new Object[size];
			for (int i = 0; i < values.length; i++) {
				values[i] = random(vars);
			}
			value = values;
		}

		return value;
	}

	private Object randomValue(Map<String, Object> vars) throws InPUTException {
		if (param.isFixed())
			return param.getFixedValue();
		else
			return random(vars);
	}

	private Object randomArray(Integer[] dimensions, Map<String, Object> vars)
			throws InPUTException {
		Object[] valueArray;
		if (dimensions[0] > 0)
			// create a container for the array of the first dimension
			valueArray = new Object[dimensions[0]];
		else
			// for now, randomly created entries without specific definition,
			// will receive minimum size.
			valueArray = new Object[1];
		// for all dimensions
		for (int i = 0; i < valueArray.length; i++) {
			// call the method again without the first dimension.
			valueArray[i] = random(
					Arrays.copyOfRange(dimensions, 1, dimensions.length), vars);
		}
		return valueArray;
	}

	protected abstract Object random(Map<String, Object> vars)
			throws InPUTException;
}