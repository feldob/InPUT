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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.model.Ranges;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.util.Q;

/**
 * NValue reflects the NValue elements in the xml files, defined in Design.xsl.
 * Numerical values are values that can be of type boolean, integer, short,
 * long, numerical, double, float.
 * 
 * @author Felix Dobslaw
 */
public class NValue extends Value<NParam> {

	private static final long serialVersionUID = -502540797291970834L;

	public NValue(final Element original, final NParam param,
			Integer[] dimensions) throws InPUTException {
		super(Q.NVALUE, param, original, dimensions, null);
		// Do not create the value in this object (lazy loading)!!!
		initElement(original);
	}

	public NValue(final Object value, final NParam param, Integer[] dimensions)
			throws InPUTException {
		this(param, dimensions, null);
		setInputValue(value);
	}

	public NValue(NParam param, Integer[] sizeArray, ElementCache elementCache)
			throws InPUTException {
		super(Q.NVALUE, param, sizeArray, elementCache);
	}

	public void setInputValue(final Object value) throws InPUTException {
		// remove the old value from the tree
		removeContent();
		// Is the value an array?...
		if (value.getClass().isArray()) {
			// in that case, extract and add the whole array/matrix...
			Object[] subValues = (Object[]) value;
			// add all array entries iteratively down the tree with index.
			for (int i = 0; i < subValues.length; i++)
				addArrayChild(i, subValues[i]);
		} else
			// ...otherwise, set its value as element attribute.
			initValueAttribute(value);
		// Finally, store the object.
		super.setInputValue(value);
	}

	private void addArrayChild(int index, Object value) throws InPUTException {
		Value<NParam> numericElement;
		// create a child element that is one dimension shorter
		Integer[] arr = Arrays.copyOfRange(dimensions, 1, dimensions.length);
		numericElement = new NValue(value, param, arr);
		// overwrite the id with an the index for global access
		numericElement.setAttribute(Q.ID_ATTR, "" + (index + 1));
		// eventually, add the child.
		addContent(numericElement);
	}

	private void initValueAttribute(Object value) throws InPUTException {
		String valueString;
		// differentiate between wrapper and plain types.
		if (param.hasWrapper())
			// with a wrapper, the 'real' value has to be retrieved
			valueString = param.invokeGetter(value).toString();
		else
			// otherwise, simply tostring it, that works for all plain types.
			valueString = value.toString();
		// now, set the value to the element.
		setAttribute(Q.VALUE_ATTR, valueString);
	}

	@SuppressWarnings("unchecked")
	protected void initElement(Element original) throws InPUTException {
		// retrieve the original value attribute
		String valueString = original.getAttributeValue(Q.VALUE_ATTR);
		if (valueString != null)
			// set it if it exists
			setAttribute(Q.VALUE_ATTR, valueString);

		// copy all the children and make them comply to InPUT
		Value<NParam> childElement;
		List<Element> originalChildren = original.getChildren();
		for (int i = 0; i < originalChildren.size(); i++) {
			// construct the child element
			childElement = new NValue(originalChildren.get(i), param,
					Arrays.copyOfRange(dimensions, 1, dimensions.length));
			// reset the id, so that it matches the position in the array
			childElement.setAttribute(Q.ID_ATTR, "" + (i + 1));
			// add the child to this element
			addContent(childElement);
		}
	}

	@Override
	protected void initRandom(Map<String, Object> vars, Object[] actualParents,
			boolean lazy) throws InPUTException {
		Object value = random(dimensions, vars);
		// for further processing, add the value to the variable map
		vars.put(getId(), value);
		// set the value attribute in case it is not an array
		if (!value.getClass().isArray())
			setAttribute(Q.VALUE_ATTR, value.toString());
		else if (param.hasWrapper())
			setInputValue(null);
		else
			setInputValue(value);
		// standard use for lazy loading, where the input value is null
	}

	@Override
	protected void initValue(Object[] actualParams) throws InPUTException {
		String valueString = getAttributeValue(Q.VALUE_ATTR);
		// retrieve the numeric of the element
		super.setInputValue(param.getNumericType().parse(valueString));
		if (param.hasWrapper()) {
			// if wrapper object -> instantiate the wrapper object
			try {
				Object value = super.getInputValue();
				super.setInputValue(param.getWrapperConstructor().newInstance(
						new Object[] { value }));
			} catch (InstantiationException e) {
				throw new InPUTException(
						getId()
								+ ": The object could not be instantiated due to some reason.",
						e);
			} catch (IllegalAccessException e) {
				throw new InPUTException(getId()
						+ ": The constructor you declared is not visible.", e);
			} catch (IllegalArgumentException e) {
				throw new InPUTException(getId()
						+ ": There is no such constructor.", e);
			} catch (InvocationTargetException e) {
				throw new InPUTException(
						getId()
								+ ": Something went wrong with the creation of the constructor.",
						e);
			}
		}
	}

	@Override
	protected Object random(Map<String, Object> vars) throws InPUTException {
		Ranges ranges = param.getEvaluatedRanges(vars);
		return Numeric.randomValue(param.getNumericType(), ranges,
				param.getRNG());
	}
}