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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.SValue;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.mapping.Complex;
import se.miun.itm.input.model.mapping.StructuralMapping;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.InPUTConstructor;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public abstract class StructuralGenerator extends
		ValueGenerator<StructuralMapping, AStruct> {

	// @lazy
	private InPUTConstructor inputConstructor;
	// @lazy
	private Boolean isEnum;
	// @lazy
	private Enum<?> enumValue;
	// @lazy
	private Class<?> superClass;

	public StructuralGenerator(AStruct param, Random rng) throws InPUTException {
		super(param, rng);
	}

	/**
	 * should only be called internally.
	 * 
	 * @return
	 * @throws InPUTException
	 */
	protected void initInPUTConstructor() throws InPUTException {
		inputConstructor = new InPUTConstructor(param, getMapping()
				.getComponentType(), getMapping().getConstructorSignature());
	}

	public boolean isInitByConstructor(String localId) throws InPUTException {
		return getInPUTConstructor().isLocalInitByConstructor(localId);
	}

	public InPUTConstructor getInPUTConstructor() throws InPUTException {
		if (inputConstructor == null)
			initInPUTConstructor();
		return inputConstructor;
	}

	public Class<?> getSuperClass() throws InPUTException {
		if (superClass == null)
			superClass = initSuperClass();
		return superClass;
	}

	@SuppressWarnings("unchecked")
	public Object initComplex(Value<?> valueElement, Object[] actualParams)
			throws InPUTException {
		Complex complex = getMapping().getComplex();
		Object complexValue = complex.newInstance();
		List<SValue> children = (List<SValue>) (List<?>) valueElement
				.getChildren();
		for (SValue sValue : children) {
			complex.invokeAdd(complexValue, sValue.getInputValue(actualParams));
		}
		return complexValue;
	}



	public String getComponentType() throws InPUTException {
		return getMapping().getComponentType();
	}

	public boolean isComplex() throws InPUTException {
		return getMapping().isComplex();
	}

	public Complex getComplex() throws InPUTException {
		return getMapping().getComplex();
	}

	protected void initIsEnum() throws InPUTException {
		if (param instanceof SChoice) {
			isEnum = ((SParam) param.getParent()).isEnum();
		} else {
			if (getMapping() != null) {
				try {
					isEnum = Class.forName(getComponentType()).isEnum();
				} catch (ClassNotFoundException e) {
					throw new InPUTException(param.getId()
							+ ": There is no such class '" + getComponentType()
							+ "'.", e);
				} catch (ClassCastException e) {
					throw new InPUTException(
							"error for design space '"
									+ param.getSpaceId()
									+ "': You have not specified a code mapping for the component of id '"
									+ param.getId() + "'.", e);
				}
			} else
				isEnum = false;
		}
	}

	public boolean isEnum() throws InPUTException {
		if (isEnum == null)
			initIsEnum();
		return isEnum;
	}

	public Object handleComplex(Map<String, Object> vars,
			Object[] actualParams, Object[] array) throws InPUTException {
		Object value;
		if (isComplex())
			value = makeComplex(array, vars, actualParams);
		else
			value = array;
		return value;
	}

	protected Object makeComplex(Object[] array, Map<String, Object> vars,
			Object[] actualParams) throws InPUTException {
		// 1) reflect the right object from the param.
		Complex mapping = getComplex();
		Object complex = mapping.newInstance();
		// 2) add the entries by invoking the right method
		for (int i = 0; i < array.length; i++) {
			mapping.invokeAdd(complex, array[i]);
		}
		return complex;
	}

	public boolean hasMapping() throws InPUTException {
		return mapping != null;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public Enum<?> getEnumValue() throws InPUTException {
		if (isEnum())
			if (enumValue == null)
				enumValue = Enum.valueOf(
						(Class<? extends Enum>) getSuperClass(),
						getComponentType());
		return enumValue;
	}

	protected StructuralMapping getMapping() throws InPUTException {
		if (mapping == null)
			throw new InPUTException("design space \"" + param.getSpaceId()
					+ "\": No mapping can be found for parameter \""
					+ param.getId() + "\".");
		if (!StructuralMapping.class.isInstance(mapping)) {
			throw new InPUTException("design space \"" + param.getSpaceId()
					+ "\": Your mapping for parameter \""
					+ param.getId() + "\" is incorrect. Have you set a type?");
		}
		return mapping;
	}

	@Override
	protected Method initSetMethod(Object parentValue) throws InPUTException {
		Method handle;
		try {
			handle = parentValue.getClass().getMethod(getSetter(),
					getSuperClass());
		} catch (NoSuchMethodException e) {
			throw new InPUTException(param.getId()
					+ ": There is no such setter method by name '"
					+ getSetter() + "' and type '" + getSuperClass()
					+ "'. Look over your code mapping file.", e);
		} catch (SecurityException e) {
			throw new InPUTException(
					param.getId()
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
					ValueGenerator.EMPTY_CLASS_ARRAY);
		} catch (NoSuchMethodException e) {
			throw new InPUTException(
					param.getId()
							+ ": There is no such getter method. Look over your code mapping file.",
					e);
		} catch (SecurityException e) {
			throw new InPUTException(
					param.getId()
							+ ": You do not have the right to call getter method by name '"
							+ getGetter() + "'.", e);
		}
		return handle;
	}

	protected Class<?> initSuperClass() throws InPUTException {
		try {
			if (!hasMapping())
				// log.warn(": No code mapping has been defined for the structural parameter '"
				// + getId() + "'.");
				return null;

			String mappingId;
			if (param instanceof SChoice && isEnum()) {
				mappingId = ((SParam) param.getParent()).getComponentType();
			} else {
				mappingId = getComponentType();
			}
			return Class.forName(mappingId);
		} catch (ClassNotFoundException e) {
			throw new InPUTException(param.getSpaceId()
					+ ": There is no such class '" + getComponentType()
					+ "' that has been defined for structural parameter '"
					+ param.getId() + "'.", e);
		}
	}

	public abstract String getValueForIndex(int index) throws InPUTException;

	@Override
	public void validateInPUT(String paramId, Object value, ElementCache elementCache) throws InPUTException {
		super.validateInPUT(paramId, value, elementCache);
		checkType(value);
	}

	private void checkType(Object value) throws InPUTException {
		Class<?> cLass = getSuperClass();
		if ((param.isArrayType() && !value.getClass().isArray()) || (!param.isArrayType() && !cLass.isInstance(value)))
			throw new InPUTException("The object \"" + value
					+ "\" is of the wrong type. \""
					+ cLass.getName() + "\" was expected, but was \""
					+ value.getClass().getName() + "\".");
	}

	@Override
	protected StructuralMapping init(Element element) throws InPUTException {
		return new StructuralMapping(element);
	}

	@Override
	public boolean initByConstructor(String paramId) {
		return mapping.containsInConstructorSignature(paramId);
	}
}