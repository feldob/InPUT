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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Element;

import se.miun.itm.input.aspects.Fixable;
import se.miun.itm.input.model.Dimensions;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.InPUTElement;
import se.miun.itm.input.model.mapping.Complex;
import se.miun.itm.input.model.mapping.EmptyMapping;
import se.miun.itm.input.model.mapping.IMapping;
import se.miun.itm.input.model.mapping.IMappings;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.util.Q;

/**
 * A parameter is an abstract meta description of all InPUT relevant aspects
 * that are represented by parameter definitions, spanning e.g. the default value ranges, its choices, its dependencies, its mappings, etc.
 * Each parameter is managed by a single container, the ParamStore. It combines the language and implementation dependant information from the code mapping and the abstract definition from the design space definitions.
 * 
 * @author Felix Dobslaw
 */
public abstract class Param extends InPUTElement implements Fixable {

	private static final long serialVersionUID = -8571051627245903785L;

	private final Set<Param> minDependencies = new HashSet<Param>();

	private final Set<Param> maxDependencies = new HashSet<Param>();

	private final Set<Param> dependees = new HashSet<Param>();

	private final Set<Param> dependencies = new HashSet<Param>();

	protected final IMapping mapping;

	private final Integer[] dimensions;

	// @LazyLoading
	private Method setHandle;

	// @LazyLoading
	private Method getHandle;

	private final String localId;

	protected final ParamStore ps;

	private final String fixed;

	public Param(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original);
		localId = original.getAttributeValue(Q.ID_ATTR);
		this.ps = ps;
		dimensions = Dimensions.derive(original);
		initFromOriginal(original);
		mapping = initMappings();
		fixed = getAttributeValue(Q.FIXED_ATTR);
	}

	public Integer[] getDimensions() {
		return dimensions;
	}

	private IMapping initMappings() {
		IMapping m = null;
		if (ps != null) {
			IMappings cm = Mappings.getInstance(ps.getId());
			if (cm != null)
				m = cm.getMapping(getId());
		}

		if (m == null)
			m = new EmptyMapping(this);
		return m;
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

	public String getDesignSpaceId() {
		return ps.getId();
	}

	public Set<Param> getMinDependencies() {
		return minDependencies;
	}

	public Set<Param> getMaxDependencies() {
		return maxDependencies;
	}

	public Set<Param> getDependees() {
		return dependees;
	}

	public void addMaxDependency(Param param) {
		maxDependencies.add(param);
		dependencies.add(param);
	}

	public void addMinDependency(Param param) {
		minDependencies.add(param);
		dependencies.add(param);
	}

	public void addDependee(Param param) {
		dependees.add(param);
	}

	public boolean makesDependant(Param param) {
		return dependees.contains(param);
	}

	public boolean dependsOn(Param param) {
		return directlyDependsOn(param) || dependsOnDependers(param);
	}

	private boolean dependsOnDependers(Param param) {
		for (Param dependant : dependencies)
			if (dependant.dependsOn(param))
				return true;
		return false;
	}

	public boolean directlyDependsOn(Param param) {
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

	public String getSetter() {
		return mapping.getSetter();
	}

	public String getGetter() {
		return mapping.getGetter();
	}

	public int getAmountDependees() {
		return dependees.size();
	}

	public int getAmountDirectDependencies() {
		return dependencies.size();
	}

	public String getLocalId() {
		return localId;
	}

	protected abstract Method initSetMethod(Object parentValue)
			throws InPUTException;

	protected abstract Method initGetMethod(Object parentValue)
			throws InPUTException;

	public String getDimsToString() {
		return Dimensions.toString(dimensions);
	}

	public abstract String getParamId();

	public boolean hasGetHandle() {
		return mapping.hasGetHandle();
	}

	public boolean hasSetHandle() {
		return mapping.hasSetHandle();
	}

	public ParamStore getParamStore() {
		return ps;
	}

	public void invokeSetter(Object parentValue, Object value)
			throws InPUTException {
		if (setHandle == null)
			setHandle = initSetMethod(parentValue);

		try {
			setHandle.invoke(parentValue, value);
		} catch (IllegalAccessException e) {
			throw new InPUTException(getId()
					+ ": You do not have access to the method '"
					+ setHandle.getName() + "'.", e);
		} catch (IllegalArgumentException e) {
			throw new InPUTException(
					getId()
							+ ": A method '"
							+ setHandle.getName()
							+ "' with the given arguments does not exist. look over your code mapping file.",
					e);
		} catch (InvocationTargetException e) {
			throw new InPUTException(getId() + ": An exception in the method '"
					+ setHandle.getName() + "' has been thrown.", e);
		}
	}

	public Object invokeGetter(Object value) throws InPUTException {

		if (getHandle == null)
			getHandle = initGetMethod(value);

		Object fieldValue;
		try {
			fieldValue = getHandle.invoke(value, AStruct.EMPTY_OBJECT_ARRAY);
		} catch (IllegalAccessException e) {
			throw new InPUTException(getId()
					+ ": You do not have access to the method '"
					+ getHandle.getName() + "'.", e);
		} catch (IllegalArgumentException e) {
			throw new InPUTException(
					getId()
							+ ": An method '"
							+ getHandle.getName()
							+ "' with the given arguments does not exist. look over your code mapping file.",
					e);
		} catch (InvocationTargetException e) {
			throw new InPUTException(getId() + ": An exception in the method '"
					+ getHandle.getName() + "' has been thrown.", e);
		}
		return fieldValue;
	}

	public Random getRNG() {
		return ps.getRNG();
	}

	@Override
	public boolean isFixed() {
		return fixed != null;
	}

	@Override
	public String getFixed() {
		return fixed;
	}

	public Complex getComplex() {
		return mapping.getComplex();
	}

	public abstract boolean isComplex();

	public abstract boolean isImplicit();

	public abstract Class<?> getInPUTClass();

	public static boolean isArrayType(Integer[] dimensions) {
		return dimensions != null && dimensions.length > 0
				&& (dimensions[0] != 0);
	}

	public boolean isArrayType() {
		return isArrayType(dimensions);
	}

}