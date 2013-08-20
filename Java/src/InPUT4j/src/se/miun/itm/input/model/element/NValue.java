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
 */package se.miun.itm.input.model.element;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.util.Q;

/**
 * NValue reflects the NValue elements in the xml files, defined in Design.xsl.
 * Numerical values are values that can be of type boolean, integer, short,
 * long, numerical, double, float.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class NValue extends Value<NParam> {

	private static final long serialVersionUID = -502540797291970834L;

	public NValue(final Element original, final NParam param, int[] dimensions)
			throws InPUTException {
		super(Q.NVALUE, param, original, dimensions, null);
		// Do not create the value in this object (lazy loading)!!!
		initFromElement(original);
	}

	public NValue(final Object value, final NParam param, int[] dimensions)
			throws InPUTException {
		this(param, dimensions, null);
		setInputValue(value);
	}

	public NValue(NParam param, int[] sizeArray, ElementCache elementCache)
			throws InPUTException {
		super(Q.NVALUE, param, sizeArray, elementCache);
	}

	protected void initFromElement(Element original) throws InPUTException {
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
		Object value = param.next(dimensions, vars);
		// for further processing, add the value to the variable map
		vars.put(getId(), value);
		// set the value attribute in case it is not an array
		param.initNumericElementFromValue(this, value);
	}

	@Override
	public void setInputValue(final Object value) throws InPUTException {
		// remove the old value from the tree
		removeContent();
		// Is the value an array?...
		if (value.getClass().isArray()) {
			// in that case, extract and add the whole array/matrix...
			setInputArray(value);
		} else if ((!getId().equals(Q.RUNTIME_VALIDATION))){
			// ...otherwise, set its value as element attribute.
			initValueAttribute(value);
		}
		// Finally, store the object.
		super.setInputValue(value);
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

	private void setInputArray(final Object value) throws InPUTException {
		// add all array entries iteratively down the tree with index.
			for (int i = 0; i < Array.getLength(value); i++)
				addArrayChild(i, Array.get(value, i));
	}

	private void addArrayChild(int index, Object value) throws InPUTException {
		Value<NParam> numericElement;
		// create a child element that is one dimension shorter
		int[] arr = Arrays.copyOfRange(dimensions, 1, dimensions.length);
		numericElement = new NValue(value, param, arr);
		// overwrite the id with an the index for global access
		numericElement.setAttribute(Q.ID_ATTR, "" + (index + 1));
		// eventually, add the child.
		addContent(numericElement);
	}
}