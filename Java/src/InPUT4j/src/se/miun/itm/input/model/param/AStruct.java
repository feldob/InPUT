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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.SValue;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.generator.StructuralGenerator;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * AStruct is an InPUT structure element that combines the common information
 * from SParam and SChoice, in order to increase code reuse.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public abstract class AStruct extends Param<StructuralGenerator> {

	private static final long serialVersionUID = 5891445520352579733L;

	private final List<Param<?>> inputChildrenList = new ArrayList<Param<?>>();

	private final Map<String, Param<?>> inputChildrenMap = new HashMap<String, Param<?>>();

	public AStruct(Element original, String designId, ParamStore ps) throws InPUTException {
		super(original, designId, ps);
		initParamChildren();
	}

	private void initParamChildren() {
		Param<?> elem;
		for (Object childElement : getChildren())
			if (childElement instanceof NParam || childElement instanceof SParam) {
				elem = (Param<?>) childElement;
				inputChildrenList.add(elem);
				inputChildrenMap.put(elem.getLocalId(), elem);
			}
	}

	public Class<?> getSuperClass() throws InPUTException {
		return generator.getSuperClass();
	}

	public List<Param<?>> getParamChildren() {
		return inputChildrenList;
	}

	public Param<?> getChildParamElement(String localChildId) {
		return inputChildrenMap.get(localChildId);
	}

	protected void addChildParam(Param<?> child) {
		inputChildrenMap.put(child.getLocalId(), child);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initValue(Value<?> value, Object[] actualParams, ElementCache elementCache) throws InPUTException {

		List<Value<Param<?>>> children = (List<Value<Param<?>>>) (List<?>) value.getChildren();
		// set the subParams and all the peers that are of type numeric.
		List<Value<? extends Param<?>>> subParamValueElements = Arrays.asList(children.toArray(new Value<?>[0]));

		String localChoiceId = value.getAttributeValue(Q.VALUE_ATTR);
		AStruct choice = getChoiceById(localChoiceId);
		if (choice == null) {
			throw new InPUTException(value.getId() + ": There is no choice element '" + localChoiceId + "' for parameter '" + getId() + "'.");
		}

		// making available possible customizable input
		if (isImplicit() && localChoiceId != null && !localChoiceId.equals(getLocalId()))
			actualParams = SParam.extendActualParams(actualParams, localChoiceId);

		actualParams = choice.enhanceActualParams(actualParams, subParamValueElements, elementCache);

		if (isStringType()) {
			Object valueString = value.getAttributeValue(Q.VALUE_ATTR);
			if (valueString == null)
				valueString = actualParams[0];
			value.setInputValue(valueString);
		} else
			reflectObject((SValue) value, actualParams, choice);

		for (Value<?> subValueE : subParamValueElements)
			// inject the child values into the members
			initIfNotConstructorInit((SValue) value, subValueE, value.getInputValue(null));

	}

	public void reflectObject(SValue sValue, Object[] actualParams, AStruct choice) throws InPUTException {
		if (isEnum().booleanValue())
			sValue.setPlainInputValue(((SChoice) choice).getEnumValue());
		else
			sValue.setPlainInputValue(choice.newInstance(actualParams));
	}

	// TODO extract an enhancer class
	public Object[] enhanceActualParams(Object[] actualParams, List<Value<?>> subParamValueElements, ElementCache elementCache) throws InPUTException {
		InPUTConstructor inputConstructor = generator.getInPUTConstructor();

		String[] formalParamIds = inputConstructor.getFormalParamIds();

		if (isStringType()) {
			if (actualParams == null) {
				actualParams = getStringTypeActualParam();
			}
		} else if (isParameterizable(actualParams, formalParamIds)) {
			if (actualParams != null && formalParamIds.length > 0)
				actualParams = Arrays.copyOfRange(actualParams, 0, formalParamIds.length);
			Object[] newParams = new Object[formalParamIds.length];
			for (int i = 0; i < formalParamIds.length; i++)
				newParams[i] = enhanceActualParam(actualParams, subParamValueElements, elementCache, inputConstructor, formalParamIds[i], i);
			actualParams = newParams;
		}
		return actualParams;
	}

	public abstract Object[] getStringTypeActualParam();

	public abstract boolean isStringType();

	private boolean isParameterizable(Object[] actualParams, String[] formalParamIds) {
		return actualParams != null || formalParamIds.length > 0;
	}

	private Object enhanceActualParam(Object[] actualParams, List<Value<? extends Param<?>>> subParamValueElements, ElementCache elementCache,
			InPUTConstructor inputConstructor, String paramId, int index) throws InPUTException {
		Object enhancedValue = null;
		try {
			if (parameterIsDefined(actualParams, index))
				enhancedValue = actualParams[index];
			else {
				if (inputConstructor.isLocalInitByConstructor(paramId))
					enhancedValue = getValueForLocalId(subParamValueElements, paramId);
				else if (inputConstructor.isGlobalIdUsedInConstructor(paramId))
					enhancedValue = getValueForGlobalParamId(subParamValueElements, paramId, elementCache);
				else if (elementCache != null)
					enhancedValue = getValueForContext(actualParams, elementCache, index, paramId);
			}
		} catch (Exception e) {
			throw new InPUTException(
					getId()
							+ ": An actual parameter with id or type '"
							+ paramId
							+ "' is missing for the initialization of the object for type '"
							+ generator.getComponentType()
							+ "'. Please ensure that your InPUT mapping file, your constructor signature, and your user input are compatible. Another alternative is, if it is a complex mapping parameter, with subentries that have a wrong mapping setup.",
					e);
		}
		return enhancedValue;
	}

	private boolean parameterIsDefined(Object[] actualParams, int index) {
		return actualParams != null && actualParams[index] != Q.BLANK;
	}

	private Object getValueForContext(Object[] actualParams, ElementCache elementCache, int i, String paramId) throws InPUTException {
		Value<?> elem = elementCache.get(paramId);
		Object result;
		if (elem != null)
			result = elem.getInputValue(actualParams);
		else {
			if (ParamUtil.isMethodContext(paramId))
				result = ParamUtil.getMethodContextValue(paramId, getId(), getParamId(), ps, elementCache);
			else
				result = actualParams[i];
		}
		return result;
	}

	private Object getValueForGlobalParamId(List<Value<? extends Param<?>>> subParamValueElements, String paramId, ElementCache elementCache)
			throws InPUTException {
		Value<? extends Param<?>> valueE = elementCache.get(paramId);
		if (valueE != null) {
			Object value = valueE.getInputValue(null);
			Param<?> param = valueE.getParam();
			value = processForExport(param, value);
			return value;
		}
		throw new InPUTException("The formal parameter '" + getParamId() + "' that has been defined for choice '" + getLocalId() + "' of parameter '"
				+ getParamId() + "' cannot be found to be of global type.");
	}

	private Object getValueForLocalId(List<Value<? extends Param<?>>> potentialActualParamsE, String localId) throws InPUTException {
		Param<?> param;
		for (Value<? extends Param<?>> valueE : potentialActualParamsE) {
			param = valueE.getParam();
			if (param.getLocalId().equals(localId)) {
				Object result = valueE.getInputValue(null);
				result = processForExport(param, result);
				return result;
			}
		}
		throw new InPUTException("The formal parameter '" + localId + "' that has been defined for choice '" + getLocalId() + "' of parameter '" + getParamId()
				+ "' cannot be found to be among the children. Is the value really set in the descriptor?");
	}

	private Object processForExport(Param<?> param, Object value) throws InPUTException {
		if (param.isArrayType())
			value = ParamUtil.packArrayForExport(param.getInPUTClass(), value, param.getDimensions().length);
		return value;
	}

	public Object newInstance(Object[] actualParams) throws InPUTException {
		return generator.getInPUTConstructor().newInstance(actualParams);
	}

	@Override
	public Class<?> getInPUTClass() throws InPUTException {
		return getSuperClass();
	}

	@Override
	protected Object nextArray(Param<?> param, int[] dimensions, Map<String, Object> vars, Object[] actualParams) throws InPUTException {
		Object[] array = (Object[]) super.nextArray(param, dimensions, vars, actualParams);
		return generator.handleComplex(vars, actualParams, array);
	}

	public Object initComplex(Value<?> valueElement, Object[] actualParams) throws InPUTException {
		return generator.initComplex(valueElement, actualParams);
	}

	public Boolean isEnum() throws InPUTException {
		return generator.isEnum();
	}

	protected boolean isInitByConstructor(String localId) throws InPUTException {
		return generator.isInitByConstructor(localId);
	}

	public InPUTConstructor getInPUTConstructor() throws InPUTException {
		return generator.getInPUTConstructor();
	}

	public abstract boolean isImplicit();

	@Override
	public void checkIfParameterSettable(String paramId) throws InPUTException {
		String[] idPath = paramId.split(Pattern.quote("."));
		if (isInitByConstructor(idPath[idPath.length - 1]))
			throw new InPUTException(
					"The parameter you try to set cannot be set that way, as it is instantiated via constructor of its parent parameter. You will have to reset the parent parameter in order to change it.");
	}

	@Override
	protected boolean isPlainValueElement(Value<?> valueElement) throws InPUTException {
		return valueElement.isPlainType();
	}

	public String getComponentType() throws InPUTException {
		return generator.getComponentType();
	}

	public abstract String getValueForIndex(int index) throws InPUTException;

	@Override
	public String getValueTypeString() {
		return Q.SVALUE;
	}

	public abstract AStruct getChoiceById(String choiceLocalId);

	public abstract Object getFixedChoice(Object value) throws InPUTException;

	public abstract boolean isComplex() throws InPUTException;

	public abstract void initIfNotConstructorInit(SValue sValue, Value<?> subValue, Object value) throws InPUTException;

	public abstract String getLocalChildIdByComponentId(String className) throws InPUTException;

	@Override
	public Class<?> getArrayType() {
		return generator.getArrayType(getDimensions());
	}
}