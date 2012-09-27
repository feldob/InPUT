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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * AStruct is an InPUT structure element that combines the common information
 * from SParam and SChoice, in order to increase code reuse.
 * 
 * @author Felix Dobslaw
 */
public abstract class AStruct extends Param {

	private static final long serialVersionUID = 5891445520352579733L;

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private final Class<?> superClass;

	private final boolean isEnum;

	private final List<Param> inputChildren = new ArrayList<Param>();

	private final Map<String, Param> inputChildrenM = new HashMap<String, Param>();

	private InPUTConstructor inputConstructor;

	public AStruct(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		superClass = initSuperClass();
		isEnum = initIsEnum();
		initParamChildren();
	}

	protected abstract boolean initIsEnum() throws InPUTException;

	private void initParamChildren() {
		Param elem;
		for (Object childElement : getChildren())
			if (childElement instanceof NParam
					|| childElement instanceof SParam) {
				elem = (Param) childElement;
				inputChildren.add(elem);
				inputChildrenM.put(elem.getLocalId(), elem);
			}
	}

	@Override
	public Method initSetMethod(Object parentValue) throws InPUTException {
		Method handle;
		try {
			handle = parentValue.getClass().getMethod(getSetter(),
					getSuperClass());
		} catch (NoSuchMethodException e) {
			throw new InPUTException(getId()
					+ ": There is no such setter method by name '"
					+ getSetter() + "' and type '" + getSuperClass()
					+ "'. Look over your code mapping file.", e);
		} catch (SecurityException e) {
			throw new InPUTException(
					getId()
							+ ": You do not have the right to call the setter method by name '"
							+ getSetter() + "'.", e);
		}
		return handle;
	}

	@Override
	protected Method initGetMethod(Object parentValue) throws InPUTException {
		Method handle;
		try {
			handle = parentValue.getClass().getMethod(getGetter(),
					EMPTY_CLASS_ARRAY);
		} catch (NoSuchMethodException e) {
			throw new InPUTException(
					getId()
							+ ": There is no such setter method. Look over your code mapping file.",
					e);
		} catch (SecurityException e) {
			throw new InPUTException(
					getId()
							+ ": You do not have the right to call getter method by name '"
							+ getGetter() + "'.", e);
		}
		return handle;
	}

	protected abstract Class<?> initSuperClass() throws InPUTException;

	public Class<?> getSuperClass() {
		return superClass;
	}

	public List<Param> getParamChildren() {
		return inputChildren;
	}

	public Param getChildParamElement(String localChildId) {
		return inputChildrenM.get(localChildId);
	}

	protected void addChildParam(Param child) {
		inputChildrenM.put(child.getLocalId(), child);
	}

	public boolean isEnum() {
		return isEnum;
	}

	/**
	 * should only be called internally.
	 * 
	 * @return
	 * @throws InPUTException
	 */
	protected void initInPUTConstructor() throws InPUTException {
		String constrString = mapping.getConstructorSignature();
		inputConstructor = new InPUTConstructor(constrString, this, ps, mapping);
	}

	public boolean isInitByConstructor(String localId) throws InPUTException {
		return inputConstructor.isLocalInitByConstructor(localId);
	}

	protected InPUTConstructor getInPUTConstructor() throws InPUTException {
		return inputConstructor;
	}

	public Object[] enhanceActualParams(Object[] actualParams,
			List<Value<? extends Param>> subParamValueElements,
			ElementCache elementCache) throws InPUTException {
		InPUTConstructor inputConstructor = getInPUTConstructor();

		String[] formalParamIds = inputConstructor.getFormalParamIds();
		if (isParameterizable(actualParams, formalParamIds)) {
			Object[] newParams = new Object[formalParamIds.length];
			for (int i = 0; i < formalParamIds.length; i++)
				newParams[i] = enhanceActualParam(actualParams,
						subParamValueElements, elementCache, inputConstructor,
						formalParamIds[i], i);
			actualParams = newParams;
		}
		return actualParams;
	}

	private boolean isParameterizable(Object[] actualParams,
			String[] formalParamIds) {
		return (actualParams != null && actualParams.length <= formalParamIds.length)
				|| formalParamIds.length > 0;
	}

	private Object enhanceActualParam(Object[] actualParams,
			List<Value<? extends Param>> subParamValueElements,
			ElementCache elementCache, InPUTConstructor inputConstructor,
			String paramId, int i) throws InPUTException {
		// prio 1 to actualParam input from user!
		Object enhancedParam = null;
		try {
			if (actualParams != null && actualParams[i] != Q.BLANK) {
				enhancedParam = actualParams[i];
			} else {
				if (inputConstructor.isLocalInitByConstructor(paramId))
					enhancedParam = getValueForLocalId(subParamValueElements,
							paramId);
				else if (inputConstructor.isGlobalIdUsedInConstructor(paramId))
					enhancedParam = getValueForGlobalParamId(
							subParamValueElements, paramId, elementCache);
				else if (elementCache != null)
					enhancedParam = getValueForContext(actualParams,
							elementCache, i, paramId);
			}
		} catch (Exception e) {
			throw new InPUTException(
					getId()
							+ ": An actual parameter with id or type '"
							+ paramId
							+ "' is missing for the initialization of the object of type '"
							+ mapping.getComponentId()
							+ "'. Please ensure that your InPUT mapping file, your constructor signature, and your user input are compatible.",
					e);
		}
		return enhancedParam;
	}

	private Object getValueForContext(Object[] actualParams,
			ElementCache elementCache, int i, String paramId)
			throws InPUTException {
		Value<?> elem = elementCache.get(paramId);
		Object result;
		if (elem != null)
			result = elem.getInputValue(actualParams);
		else {
			if (ParamUtil.isMethodContext(paramId))
				result = ParamUtil.getMethodContextValue(paramId, getId(),
						getParamId(), ps, elementCache);
			else
				result = actualParams[i];
		}
		return result;
	}

	private Object getValueForGlobalParamId(
			List<Value<? extends Param>> subParamValueElements, String paramId,
			ElementCache elementCache) throws InPUTException {
		Value<? extends Param> valueE = elementCache.get(paramId);
		if (valueE != null)
			return valueE.getInputValue(null);
		throw new InPUTException("The formal parameter '" + getParamId()
				+ "' that has been defined for choice '" + getLocalId()
				+ "' of parameter '" + getParamId()
				+ "' cannot be found to be of global type.");
	}

	private Object getValueForLocalId(
			List<Value<? extends Param>> potentialActualParamsE, String localId)
			throws InPUTException {
		for (Value<? extends Param> valueE : potentialActualParamsE)
			if (valueE.getParam().getLocalId().equals(localId))
				return valueE.getInputValue(null);
		throw new InPUTException(
				"The formal parameter '"
						+ localId
						+ "' that has been defined for choice '"
						+ getLocalId()
						+ "' of parameter '"
						+ getParamId()
						+ "' cannot be found to be among the children. Is the value really set in the descriptor?");
	}

	public Object newInstance(Object[] actualParams) throws InPUTException {
		return getInPUTConstructor().newInstance(actualParams);
	}

	@Override
	public Class<?> getInPUTClass() {
		return getSuperClass();
	}
}