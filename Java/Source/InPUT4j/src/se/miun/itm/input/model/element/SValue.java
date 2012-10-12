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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Content;
import org.jdom2.Element;

import se.miun.itm.input.model.Dimensions;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * SValues are complex values that require a code mapping which allows the
 * platform and language dependent definition of components for the abstract
 * design and design space descriptors. In Java, these mappings are between
 * parameter id Strings and class ids.
 * 
 * @author Felix Dobslaw
 * 
 */
public class SValue extends Value<SParam> {

	private static final long serialVersionUID = -3106504874487719602L;
	
	private final SChoice fixedChoice;

	public SValue(Object value, SParam param, Integer[] sizeArray,
			ElementCache elementCache) throws InPUTException {
		this(param, sizeArray, elementCache);
		setInputValue(value);
	}

	SValue(SParam param, Integer[] sizeArray, ElementCache elementCache)
			throws InPUTException {
		this(param, sizeArray, elementCache, null);
	}

	private SValue(SParam param, Integer[] sizeArray, ElementCache elementCache, SChoice fixedChoice) throws InPUTException {
		super(Q.SVALUE, param, sizeArray, elementCache);
		this.fixedChoice = fixedChoice;
	}

	public SValue(SChoice choice, Integer[] dimensions, ElementCache elementCache) throws InPUTException {
		this(choice.getParam(), dimensions, elementCache, choice);
	}

	public SValue(Element originalChoice, SParam param, Integer[] sizeArray,
			ElementCache elementCache) throws InPUTException {
		super(Q.SVALUE, param, originalChoice, sizeArray, elementCache);
		fixedChoice = null;
		setInputElement(originalChoice);
	};

	public SValue(Object value, SParam param, Integer[] dimensions)
			throws InPUTException {
		this(param, dimensions, null);
		setInputValue(value);
	}

	@Override
	public void setInputValue(Object value) throws InPUTException {
		// remove all existing sub-values
		removeContent();
		if (value != null) {
			// TODO this addArrayChild does not work as it points to a wrong
			// impl with choices as input!
			if (value.getClass().isArray()) {
				// in that case, extract and add the whole array/matrix...
				Object[] subValues = (Object[]) value;
				// add all array entries iteratively down the tree with index.
				for (int i = 0; i < subValues.length; i++)
					addArrayChild(i, subValues[i],
							new HashMap<String, Object>());
			} else {
				// ...otherwise, set its attributes as element attribute.
				String localId = param.getLocalChildIdByComponentId(value
						.getClass().getName());
				// reset the element value attribute
				setAttribute(Q.VALUE_ATTR, localId);
				// find the structChoice element for the object value
				AStruct choice = param.getChoiceById(localId);
				for (Param subParam : param.getParamChildren())
					if (!(subParam instanceof SChoice))
						addSubValue(value, subParam);
				for (Param subParam : choice.getParamChildren())
					addSubValue(value, subParam);

			}

		} else
			setAttribute(Q.VALUE_ATTR, "null");
		// reset the object value
		super.setInputValue(value);
	}

	private void addArrayChild(int index, Object value, Map<String, Object> vars)
			throws InPUTException {
		SValue element;
		// create a child element that is one dimension shorter

		Integer[] dims;
		if (dimensions.length > 1)
			dims = Arrays.copyOfRange(dimensions, 1, dimensions.length);
		else
			dims = Dimensions.DEFAULT_DIM;

		if (value instanceof SChoice) {
			element = new SValue(param, dims, elementCache);
			// overwrite the id with an the index for global access
			element.setAttribute(Q.ID_ATTR, "" + (index + 1));
			// eventually, add the child.
			addContent(element);
			element.initChoice((SChoice) value, vars);
		}
	}

	// build the whole tree
	protected void setInputElement(Element originalChoice)
			throws InPUTException {
		String localChoiceId = originalChoice.getAttributeValue(Q.VALUE_ATTR);

		if (localChoiceId != null)
			setAttribute(Q.VALUE_ATTR, localChoiceId);

		// for all children get name, value and id attribute
		Param subParam;
		Value<? extends Param> newSubValue;
		List<Element> choiceSubs = originalChoice.getChildren();
		for (Element originalChoiceSub : choiceSubs) {
			// find the matching parameter to the value
			// subParam = param.getChildParamElement(subValueO.getID);
			subParam = retrieveParamForValueE(originalChoiceSub, originalChoice);
			// TODO is this really desired for a user, a default random init?
			if (subParam == null) {
				// there is no such element.initRandom
				newSubValue = ValueFactory.constructElementByElement(
						originalChoiceSub, param,
						Arrays.copyOfRange(dimensions, 1, dimensions.length),
						elementCache);
			} else
				// construct a new ValueElement subValue of the correct type
				newSubValue = ValueFactory.constructElementByElement(
						originalChoiceSub, subParam,
						Dimensions.derive(subParam), elementCache);
			// add the subValue to this value
			addContent(newSubValue);
		}
	}

	private Param retrieveParamForValueE(Element originalChild,
			Element originalChoice) throws InPUTException {
		// get the param for sub value. A sub value can be of two types
		String choiceLocalId = originalChoice.getAttributeValue(Q.VALUE_ATTR);
		String subParamId = originalChild.getAttributeValue(Q.ID_ATTR);
		// this is a subentry of an array type, get back to the parent type.
		if (choiceLocalId == null && ParamUtil.isIntegerString(subParamId))
			return null;
		else {
			// either its child of the choice or the param.
			Param subParam = param.getChildParamElement(subParamId);
			if (subParam == null) {
				// chances are its a neighbor param of the parent. lets get it
				// via lookup!
				subParam = param.getChildParamElement(subParamId);
				if (subParam == null) {
					AStruct choice = param.getChoiceById(choiceLocalId);
					if (choice == null) {
						throw new InPUTException("Configuration error for parameter \""+getId()+"\". Potential sources: " 
								+ "1) You set a sub-parameter \"" +subParamId +"\" to the configuration, which does not exist. 2) "
								+ "you misspelled the choice entry in your design: \"" + choiceLocalId
								+ "\". In any case, make sure that the identfiers in design (value), design space and mapping (choice or parameter) match.");
					}
					subParam = choice.getChildParamElement(subParamId);
				}
			}
			return subParam;
		}
	}

	private void addSubValue(Object thisNewValue, Param subParam)
			throws InPUTException {
		if (subParam.hasGetHandle()) {
			Object subValue = subParam.invokeGetter(thisNewValue);
			// call set param for the respective subparam
			addContent(ValueFactory.constructElementByValue(subValue, subParam,
					dimensions, elementCache));
		}
	}

	protected void initChoice(AStruct choice, Map<String, Object> vars)
			throws InPUTException {
		// retrieve value type
		String valueType = choice.getAttributeValue(Q.ID_ATTR);
		// set the value type for the element
		setAttribute(Q.VALUE_ATTR, valueType);
		// init all sub-params

		initSubParams(param, param.getParamChildren(), vars); // peers

		if (choice instanceof SChoice) {
			initSubParams(choice, choice.getParamChildren(), vars); // children
		}
	}

	private void initSubParams(Element parent,
			List<? extends Param> childrenToInit, Map<String, Object> vars)
			throws InPUTException {

		Value<?> subValueE;
		for (Param subParamElement : childrenToInit) {
			subValueE = ValueFactory.constructValueElement(subParamElement, elementCache);
			subValueE.initRandom(vars, null, true);
			addContent(subValueE);
		}
	}

	@Override
	protected void initRandom(Map<String, Object> vars, Object[] actualParams,
			boolean lazy) throws InPUTException {
		// the result is either structChoice or an array of struct-choice!
		Object value = random(dimensions, vars);
			if (fixedChoice != null) {
			value = initFixedChoice(value);
		}

		if (value.getClass().isArray()) {
			// in that case, extract and add the whole array/matrix...
			Object[] subValues = (Object[]) value;
			// add all array entries iteratively down the tree with index.
			for (int i = 0; i < subValues.length; i++)
				addArrayChild(i, subValues[i], vars);
		} else
			// ...otherwise, set its value as element attribute.
			initChoice((AStruct) value, vars);
		// Finally, store the object.

		if (!lazy)
			init(actualParams);
	}

	private Object initFixedChoice(Object value) {
		if (!value.getClass().isArray())
			return fixedChoice;
		
		Object[] current = (Object[])value;
		for (int i = 0; i < current.length; i++) {
			initFixedChoice(current[i]);
		}
		return current;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Element addContent(Content subValueE) {
		Object value = getInputValue();
		if (value != null) {
			Value<? extends Param> subValue = ((Value<? extends Param>) subValueE);
			try {
				String localId = getAttributeValue(Q.VALUE_ATTR);
				if (!param.getChoiceById(localId).isInitByConstructor(
						subValue.getLocalId())
						&& subValue.getParam().hasSetHandle())
					subValue.injectOnParent(value);
			} catch (InPUTException e) {
				warning(subValueE);
			}
		}
		return super.addContent(subValueE);
	}

	// signaling method for logging purpose
	private void warning(Content subValueE) {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initValue(Object[] actualParams) throws InPUTException {

		List<Value<Param>> children = (List<Value<Param>>)(List<?>)getChildren();
		// set the subParams and all the peers that are of type numeric.
		List<Value<? extends Param>> subParamValueElements = Arrays
				.asList(children.toArray(new Value<?>[0]));

		String localChoiceId = getAttributeValue(Q.VALUE_ATTR);
		AStruct choice = param.getChoiceById(localChoiceId);
		if (choice == null) {
			throw new InPUTException(getId() + ": There is no choice element '"
					+ localChoiceId + "' for parameter '" + param.getId()
					+ "'.");
		}

		// making available possible customizable input
		if (param.isImplicit() && localChoiceId != null && !localChoiceId.equals(param.getLocalId()))
			actualParams = extendActualParams(actualParams, localChoiceId);

		actualParams = choice.enhanceActualParams(actualParams,
				subParamValueElements, elementCache);
		//TODO process actualParams arrays so that it ain't remains an object array
		reflectObject(actualParams, choice);

		for (Value<? extends Param> subValueE : subParamValueElements)
			// inject the child values into the members
			if (!choice.isInitByConstructor(subValueE.getLocalId())
					&& subValueE.getParam().hasSetHandle())
				subValueE.injectOnParent(getInputValue());
	}

	private Object[] extendActualParams(Object[] actualParams,
			String localChoiceId) {
		Object[] newParams;
		if (actualParams != null) {
			newParams = new Object[actualParams.length + 1];
			for (int i = 0; i < actualParams.length; i++) {
				newParams[i] = actualParams[i];
			}
		} else {
			newParams = new Object[1];
		}

		newParams[newParams.length - 1] = localChoiceId;
		return newParams;
	}

	private void reflectObject(Object[] actualParams, AStruct choice)
			throws InPUTException {
		if (param.isEnum())
			super.setInputValue(((SChoice) choice).getEnumValue());
		else
			super.setInputValue(choice.newInstance(actualParams));
	}

	@Override
	protected Object random(Map<String, Object> vars) throws InPUTException {
		return param.nextChoice();
	}
}