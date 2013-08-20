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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Content;
import org.jdom2.Element;

import se.miun.itm.input.model.DimensionHelper;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * SValues are complex values that require a code mapping which allows the platform and language dependent definition of components for the
 * abstract design and design space descriptors. In Java, these mappings are between parameter id Strings and class ids.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class SValue extends Value<AStruct> {

	private static final long serialVersionUID = -3106504874487719602L;
	
	public SValue(Object value, AStruct param, int[] sizeArray, ElementCache elementCache) throws InPUTException {
		this(param, sizeArray, elementCache);
		setInputValue(value);
	}

	SValue(AStruct param, int[] sizeArray, ElementCache elementCache) throws InPUTException {
		super(Q.SVALUE, param, sizeArray, elementCache);
	}

	public SValue(Element originalChoice, AStruct param, int[] sizeArray, ElementCache elementCache) throws InPUTException {
		super(Q.SVALUE, param, originalChoice, sizeArray, elementCache);
		setInputElement(originalChoice);
	};

	public SValue(Object value, AStruct param, int[] dimensions) throws InPUTException {
		this(param, dimensions, null);
		setInputValue(value);
	}

	private void initArrayChild(int index, Object value, Map<String, Object> vars) throws InPUTException {
		SValue element;
		// create a child element that is one dimension shorter

		int[] dims;
		if (dimensions.length > 1)
			dims = Arrays.copyOfRange(dimensions, 1, dimensions.length);
		else
			dims = DimensionHelper.DEFAULT_DIM;

		element = new SValue(param, dims, elementCache);
		// overwrite the id with an the index for global access
		element.setAttribute(Q.ID_ATTR, "" + (index + 1));
		// eventually, add the child.
		addContent(element);

		if (value instanceof SChoice)
			element.initChoice((SChoice) value, vars);
		else
			element.initRandom(vars, null, true);
	}

	// build the whole tree
	protected void setInputElement(Element originalChoice) throws InPUTException {
		String localChoiceId = originalChoice.getAttributeValue(Q.VALUE_ATTR);

		if (localChoiceId != null)
			setAttribute(Q.VALUE_ATTR, localChoiceId);

		// for all children get name, value and id attribute
		Param<?> subParam;
		Value<?> newSubValue;
		List<Element> choiceSubs = originalChoice.getChildren();
		for (Element originalChoiceSub : choiceSubs) {
			// find the matching parameter to the value
			// subParam = param.getChildParamElement(subValueO.getID);
			subParam = ParamUtil.retrieveParamForValueE(param, originalChoiceSub, originalChoice);
			if (subParam == null) {
				// there is no such element.initRandom
				newSubValue = ValueFactory.constructElementByElement(originalChoiceSub, param,
						Arrays.copyOfRange(dimensions, 1, dimensions.length), elementCache);
			} else
				// construct a new ValueElement subValue of the correct type
				newSubValue = ValueFactory.constructElementByElement(originalChoiceSub, subParam, DimensionHelper.derive(subParam),
						elementCache);
			// add the subValue to this value
			addContent(newSubValue);
		}
	}

	@Override
	public Element addContent(Content subValueE) {
		Object value;
		try {
			value = getInputValue();
			if (value != null) {
				Value<?> subValue = ((Value<?>) subValueE);
				param.initIfNotConstructorInit(this, subValue, value);
			}
		} catch (InPUTException e) {
			warning(subValueE);
		}
		return super.addContent(subValueE);
	}

	// signaling method for logging purpose
	private void warning(Content subValueE) {
	}

	protected void initChoice(AStruct choice, Map<String, Object> vars) throws InPUTException {
		// retrieve value type
		String valueType = retrieveValue(choice);
		// set the value type for the element
		setAttribute(Q.VALUE_ATTR, valueType);
		// init all sub-params

		AStruct param = this.param;
		if (this.param instanceof SChoice)
			param = (SParam)param.getParent();
		initSubParams(param, param.getParamChildren(), vars); // peers

		if (choice instanceof SChoice) {
			initSubParams(choice, choice.getParamChildren(), vars); // children
		}
	}

	private String retrieveValue(AStruct choice) {
		String valueType;
		if (choice.getAttributeValue(Q.TYPE_ATTR)!= null && choice instanceof SParam && choice.getAttributeValue(Q.TYPE_ATTR).equals(Q.STRING) && choice.getAttributeValue(Q.FIXED_ATTR)!=null)
			valueType = choice.getAttributeValue(Q.FIXED_ATTR);
		else
			valueType = choice.getAttributeValue(Q.ID_ATTR);
		return valueType;
	}

	private void initSubParams(Element parent, List<Param<?>> childrenToInit, Map<String, Object> vars) throws InPUTException {

		Value<?> subValueE;
		for (Param<?> subParamElement : childrenToInit) {
			subValueE = ValueFactory.constructValueElement(subParamElement, elementCache);
			if (vars.containsKey(subParamElement.getId()))
				subValueE.setInputValue(vars.get(subParamElement.getId()));
			else if (vars.containsKey(subParamElement.getLocalId()))
				subValueE.setInputValue(vars.get(subParamElement.getLocalId()));
			else
				subValueE.initRandom(vars, null, true);
			addContent(subValueE);
		}
	}

	@Override
	protected void initRandom(Map<String, Object> vars, Object[] actualParams, boolean lazy) throws InPUTException {
		// the result is either structChoice or an array of struct-choice!
		Object value = param.next(dimensions, vars);

		if (value.getClass().isArray()) {
			// in that case, extract and add the whole array/matrix...
			Object[] subValues = (Object[]) value;
			// add all array entries iteratively down the tree with index.
			for (int i = 0; i < subValues.length; i++)
				initArrayChild(i, subValues[i], vars);
		} else
			// ...otherwise, set its value as element attribute.
			initChoice((AStruct) value, vars);
		// Finally, store the object.

		if (!lazy)
			param.init(this, actualParams, elementCache);
	}

	private void addArrayChild(int index, Object value, Map<String, Object> vars) throws InPUTException {
		// create a child element that is one dimension shorter

		int[] dims;
		if (dimensions.length > 1)
			dims = Arrays.copyOfRange(dimensions, 1, dimensions.length);
		else
			dims = DimensionHelper.DEFAULT_DIM;

		// if (value instanceof SChoice) {
		SValue element = new SValue(value, param, dims);
		// overwrite the id with an the index for global access
		element.setAttribute(Q.ID_ATTR, "" + (index + 1));
		// eventually, add the child.
		addContent(element);

		// element.initChoice((SChoice) value, vars);
		// }
	}

	@Override
	public void setInputValue(Object value) throws InPUTException {
		// remove all existing sub-values
		removeContent();
		setValueString(value);
		initChildren(value);
		// set the value field
		super.setInputValue(value);
	}

	private void initChildren(Object value) throws InPUTException {
		if (!param.isEnum() && value != null) {
			if (value.getClass().isArray()) {
				initArray(value);
			} else if (param instanceof SParam && param.isComplex()) {
				// we are having a complex type here, not supported yet!
			} else {
				// ...otherwise, set its attributes as element attribute.
				initChoice(value, value.getClass().getName());
			}
		}
	}

	private void initArray(Object value) throws InPUTException {
		// in that case, extract and add the whole array/matrix...
		Object[] subValues = (Object[]) value;
		// add all array entries iteratively down the tree with index.
		for (int i = 0; i < subValues.length; i++)
			addArrayChild(i, subValues[i], new HashMap<String, Object>());
	}

	private void initChoice(Object value, String className) throws InPUTException {
		String localId = param.getLocalChildIdByComponentId(className);
		
		// find the structChoice element for the object value
		AStruct choice = param.getChoiceById(localId);
		if (choice == null)
			throw new InPUTException("There is no choice in parameter '" + param.getId() + "' for the given class name: " + className);

		for (Param<?> subParam : param.getParamChildren())
			if (!(subParam instanceof SChoice))
				addSubValue(value, subParam);
		if (!choice.equals(param))
			for (Param<?> subParam : choice.getParamChildren())
				addSubValue(value, subParam);
	}

	private void setValueString(Object value) throws InPUTException {
		String valueString = null;
		if (param.isEnum()) {
			valueString = param.getLocalChildIdByComponentId(value.toString());
		} else if (param.isStringType())
			valueString = value.toString();
		else if (value != null && !value.getClass().isArray() && !(param instanceof SParam && param.isComplex()))
			valueString = param.getLocalChildIdByComponentId(value.getClass().getName());

		if (valueString != null)
			setAttribute(Q.VALUE_ATTR, valueString);
	}

	private void addSubValue(Object thisNewValue, Param<?> subParam) throws InPUTException {
		if (subParam.hasGetHandle()) {
			Object subValue = subParam.invokeGetter(thisNewValue);
			// call set param for the respective subparam
			addContent(ValueFactory.constructElementByValue(subValue, subParam, dimensions, elementCache));
		}
	}

	public void setPlainInputValue(Object value) throws InPUTException {
		super.setInputValue(value);
	}
}