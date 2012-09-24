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
package se.miun.itm.input.model.mapping;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.util.Q;

/**
 * The standard implementation for a concrete, user defined, mapping in InPUT. 
 * @author Felix Dobslaw
 *
 */
public class Mapping extends AMapping {

	private final String componentId;
	private final String constrSign;
	private final Wrapper wrapper;
	private final Complex complex;
	private final IMappings mappings;
	private final Set<IMapping> dependees = new HashSet<IMapping>();
	private final Set<String> signChops;

	public Mapping(Element mapping, IMappings mappings)
			throws InPUTException {
		super(mapping.getAttributeValue(Q.ID_ATTR), mapping
				.getAttributeValue(Q.SET_ATTR), mapping
				.getAttributeValue(Q.GET_ATTR));
		componentId = mapping.getAttributeValue(Q.TYPE_ATTR);
		wrapper = initWrapper(mapping);
		complex = initComplex(mapping);
		constrSign = initConstrSignature(mapping);
		signChops = initSignatureChops();
		this.mappings = mappings;
	}

	public Mapping(String id, IMapping mapping) {
		super(id, mapping.getSetter(), mapping.getGetter());
		componentId = mapping.getComponentId();
		wrapper = mapping.getWrapper();
		complex = mapping.getComplex();
		constrSign = mapping.getConstructorSignature();
		signChops = initSignatureChops();
		this.mappings = mapping.getCodeMappings();
	}

	private Set<String> initSignatureChops() {
		Set<String> chops;
		if (constrSign != null) {
			String[] entries = constrSign.split(Pattern.quote(" "));
			chops = new HashSet<String>(Arrays.asList(entries));
		} else
			chops = new HashSet<String>();
		return chops;
	}

	private String initConstrSignature(Element mapping) {
		if (wrapper == null)
			return mapping.getAttributeValue(Q.CONSTR_ATTR);
		else
			return wrapper.getConstructorSignature();
	}


	@SuppressWarnings("unchecked")
	private Complex initComplex(Element mapping) throws InPUTException {
		List<Element> children = mapping.getChildren();
		Element complex;
		if (children.size() > 0) {
			complex = children.get(0);
			if (complex.getName().equals(Q.COMPLEX))
				return new Complex(complex, mapping);
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Wrapper initWrapper(Element mapping) throws InPUTException {
		List<Element> children = mapping.getChildren();
		Element wrap;
		if (children.size() > 0) {
			wrap = children.get(0);
			if (wrap.getName().equals(Q.WRAPPER))
				return new Wrapper(localId, wrap, mapping);
		}

		return null;
	}

	public Class<?> getWrapperClass() {
		return wrapper.getWrapperClass();
	}

	@Override
	public String getComponentId() {
		return componentId;
	}

	@Override
	public String getConstructorSignature() {
		return constrSign;
	}

	@Override
	public String getWrapperGetter() {
		return wrapper.getGetter();
	}

	@Override
	public String getWrapperSetter() {
		return wrapper.getSetter();
	}

	@Override
	public boolean hasWrapper() {
		return wrapper != null;
	}

	@Override
	public Constructor<?> getWrapperConstructor(Numeric numType)
			throws InPUTException {
		return wrapper.getWrapperConstructor(numType);
	}

	@Override
	public IMappings getCodeMappings() {
		return mappings;
	}

	@Override
	public void addDependee(IMapping dependee) {
		dependees.add(dependee);
	}

	@Override
	public boolean isIndependent() {
		return dependees.isEmpty();
	}

	@Override
	public boolean dependsOn(IMapping other) {
		return dependees.contains(other) || dependeesDependOn(other);
	}

	private boolean dependeesDependOn(IMapping other) {
		for (IMapping dependant : dependees)
			if (dependant.dependsOn(other))
				return true;
		return false;
	}

	/**
	 * important note: dependability is only relevant for the global order of the 
	 * parameter init, and therefore, a parameter is NOT dependent on its children,
	 * meaning that local dependencies are not of interest here.
	 */
	@Override
	public boolean containsInConstructorSignature(IMapping mapping) {
		if (signChops.contains(mapping.getId()))
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return getId() + "[constructor=\"" + constrSign +"\"]";
	}
	
	@Override
	public int compareTo(IMapping o) {
		if (dependsOn(o))
			return 1;
		if (o.dependsOn(this))
			return -1;
		return 0;
	}

	@Override
	public List<IMapping> getDependees() {
		return new ArrayList<IMapping>(dependees);
	}

	@Override
	public Wrapper getWrapper() {
		return wrapper;
	}

	@Override
	public boolean isComplex() {
		return complex != null;
	}

	@Override
	public Complex getComplex() {
		return complex;
	}
}