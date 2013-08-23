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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;

import se.miun.itm.input.aspects.Dependable;
import se.miun.itm.input.model.DimensionHelper;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.InPUTElement;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.element.ValueFactory;
import se.miun.itm.input.model.param.generator.IValueGenerator;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * A parameter is an abstract meta description of all InPUT relevant aspects
 * that are represented by parameter definitions, spanning e.g. the default
 * value ranges, its choices, its dependencies, its mappings, etc. Each
 * parameter is managed by a single container, the ParamStore. It combines the
 * language and implementation dependent information from the code mapping and
 * the abstract definition from the design space definitions.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public abstract class Param<AGenerator extends IValueGenerator> extends
		InPUTElement {

	private static final long serialVersionUID = -8571051627245903785L;

	private final Set<Param<?>> minDependencies = new HashSet<Param<?>>();

	private final Set<Param<?>> maxDependencies = new HashSet<Param<?>>();

	private final Set<Param<?>> dependees = new HashSet<Param<?>>();

	private final Set<Param<?>> dependencies = new HashSet<Param<?>>();

	private final int[] dimensions;

	protected final ParamStore ps;

	protected AGenerator generator;

	public Param(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original);
		checkParameterIdValidity(original);
		this.ps = ps;
		dimensions = DimensionHelper.derive(original);
		initFromOriginal(original);
		generator = initGenerator(false);
	}

	private void checkParameterIdValidity(Element original) {
		if (this instanceof SChoice)
				return;

		String id = original.getAttributeValue(Q.ID_ATTR);
		if (id.contains("."))
			throw new IllegalArgumentException(
					"Illegal parameter identifier: '" + id
							+ "'. A parameter id may not contain dots.");
	}

	public int[] getDimensions() {
		return dimensions;
	}

	private void initFromOriginal(Element original) {
		// init attributes
		setNamespace(original.getNamespace());
		setName(original.getName());
		Attribute attr;
		for (Object content : original.getAttributes()) {
			attr = (Attribute) content;
			setAttribute(attr.getName(), attr.getValue());
		}

		// move children
		Element childE;
		Object[] children = original.getChildren().toArray();
		for (Object child : children) {
			childE = (Element) child;
			original.removeContent(childE);
			addContent(childE);
		}

		// attach to parent
		Element parent = original.getParentElement();
		parent.removeContent(original);
		parent.addContent(this);
	}

	public void init(Value<?> valueElement, Object[] actualParams,
			ElementCache elementCache) throws InPUTException {
		if (isPlainValueElement(valueElement))
			initValue(valueElement, actualParams, elementCache);
		else if (valueElement.getParam().isArrayType())
			initArray(valueElement, actualParams);
	}

	protected abstract boolean isPlainValueElement(Value<?> valueElement)
			throws InPUTException;

	public abstract void initValue(Value<?> value, Object[] actualParams,
			ElementCache elementCache) throws InPUTException;

	protected static void initArray(Value<?> element, Object[] actualParams)
			throws InPUTException {
		// make the return value an array of appropriate size
		List<Element> children = element.getChildren();

		Object value = element.getInputValue();
		if (value == null)
			value = new Object[children.size()];
		// container for the lower dimensional value entries
		Value<?> subE;
		// for all children run the casting again.
		for (int i = 0; i < children.size(); i++) {
			subE = (Value<?>) children.get(i);
			// get object for numeric element and set as child
			Array.set(value, i, subE.getInputValue(actualParams));
		}
		element.setInputValue(value);
	}

	public String getSpaceId() {
		if (ps != null)
			return ps.getId();
		return "no Id";
	}

	public Set<Param<?>> getMinDependencies() {
		return minDependencies;
	}

	public Set<Param<?>> getMaxDependencies() {
		return maxDependencies;
	}

	public Set<Param<?>> getDependees() {
		return dependees;
	}

	public void addMaxDependency(Param<?> param) throws InPUTException {
//		checkCircularDependency(param);
		maxDependencies.add(param);
		dependencies.add(param);
	}

	public void addMinDependency(Param<?> param) throws InPUTException {
//		checkCircularDependency(param);
		minDependencies.add(param);
		dependencies.add(param);
	}
//	
//	private void checkCircularDependency(Param<?> param) throws InPUTException {
//		if (dependees.contains(param))
//			throw new InPUTException("There is a circular dependency between parameter '" + getId() + "' and '"+ param.getId()+ "'. It has to be resolved.");
//	}

	public void addDependee(Param<?> param) throws InPUTException {
//		if (dependencies.contains(param))
//			throw new InPUTException("There is a circular dependency between parameter '" + getId() + "' and '"+ param.getId()+ "'. It has to be resolved.");
			
		dependees.add(param);
	}

	public boolean makesDependant(Param<?> param) {
		return dependees.contains(param);
	}

	public boolean dependsOn(Param<?> param) {
		return directlyDependsOn(param) || dependsOnDependers(param);
	}

	private boolean dependsOnDependers(Param<?> param) {
		for (Param<?> dependant : dependencies)
			if (dependant.dependsOn(param))
				return true;
		return false;
	}

	public boolean directlyDependsOn(Param<?> param) {
		return param.makesDependant(this);
	}

	public boolean isMinDependent() {
		return !minDependencies.isEmpty();
	}

	public boolean isMaxDependent() {
		return !maxDependencies.isEmpty();
	}

	public boolean isIndependant() {
		return dependencies.isEmpty();
	}

	public int getAmountDependees() {
		return dependees.size();
	}

	public int getAmountDirectDependencies() {
		return dependencies.size();
	}

	public String getLocalId() {
		return getAttributeValue(Q.ID_ATTR);
	}

	public String getDimsToString() {
		return DimensionHelper.toString(dimensions);
	}

	public abstract String getParamId();

	public boolean hasGetHandle() {
		return generator.hasGetHandle();
	}

	protected boolean hasSetHandle() {
		return generator.hasSetHandle();
	}

	public ParamStore getParamStore() {
		return ps;
	}

	protected abstract Class<?> getInPUTClass() throws InPUTException;

	public static boolean isArrayType(int[] dimensions) {
		return dimensions != null && dimensions.length > 0
				&& (dimensions[0] != 0);
	}

	public boolean isArrayType() {
		return isArrayType(dimensions);
	}

	public void validateInPUT(String paramId, Object value,
			ElementCache elementCache) throws InPUTException {
		if (value == null)
			throw new InPUTException(getId()
					+ ": Null value setting is not supported.");
		generator.validateInPUT(paramId, value, elementCache);
	}

	protected abstract AGenerator initGenerator(boolean initRanges)
			throws InPUTException;

	public void invokeSetter(Object parentValue, Object value)
			throws InPUTException {
		generator.invokeSetter(parentValue, value);
	}

	public Object next(int[] dimensions, Map<String, Object> vars)
			throws InPUTException {
		return generator.next(dimensions, vars);
	}

	// TODO move stuff to the generator!
	public Object next(int[] sizeArray, Map<String, Object> vars,
			Object[] actualParams) throws InPUTException {
		Object value = nextNext(sizeArray, vars, actualParams);
		return ParamUtil.packArrayForExport(getInPUTClass(), value,
				sizeArray.length);
	}

	private Object nextNext(int[] sizeArray, Map<String, Object> vars,
			Object[] actualParams) throws InPUTException {
		Object value;
		if (ParamUtil.isDimensionalArray(sizeArray)) {
			value = nextArray(this, sizeArray, vars, actualParams);
		} else {
			value = nextElement(getId(), DimensionHelper.DEFAULT_DIM, vars,
					actualParams).getInputValue(actualParams);
		}
		return value;
	}

	public Value<? extends Param<?>> nextElement(String paramId,
			int[] sizeArray, Map<String, Object> vars, Object[] actualParents)
			throws InPUTException {
		return ValueFactory.constructRandomElement(ps.getParam(paramId),
				sizeArray, vars, actualParents, null);
	}

	protected Object nextArray(Param<?> param, int[] dimensions,
			Map<String, Object> vars, Object[] actualParams)
			throws InPUTException {
		if (dimensions[0] < 0)
			dimensions[0] = 1;

		Object[] array = new Object[dimensions[0]];

		for (int i = 0; i < array.length; i++)
			array[i] = nextNext(
					Arrays.copyOfRange(dimensions, 1, dimensions.length), vars,
					actualParams);
		return array;
	}

	public void injectOnParent(Value<?> childElement, Object parentValue)
			throws InPUTException {
		Object value = childElement.getInputValue(null);
		invokeSetter(parentValue, value);
	}

	public abstract Object getValueForString(String stringValue)
			throws InPUTException;

	public abstract void checkIfParameterSettable(String paramId)
			throws InPUTException;

	public Object packArrayForExport(Value<?> element, Object value)
			throws InPUTException {
		return ParamUtil.packArrayForExport(getInPUTClass(), value,
				element.getDimensions());
	}

	public Object invokeGetter(Object thisNewValue) throws InPUTException {
		return generator.invokeGetter(thisNewValue);
	}

	public boolean initByConstructor(String paramId) {
		return generator.initByConstructor(paramId);
	}

	public abstract String getValueTypeString();

	public abstract void setFixed(String value) throws InPUTException;

	public abstract boolean isFixed();

	public abstract String getFixedValue() throws InPUTException;
}