package se.miun.itm.input.model.mapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom2.Attribute;
import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public class StructuralMapping extends Mapping {

	private final Complex complex;
	
	private final String constructorSignature;
	
	private final Set<String> signChops;
	
	private final String componentType;

	private StructuralMapping(Element mapping, String componentType, String constructorString) throws InPUTException{
		super(mapping);
		complex = initComplex(mapping);
		this.componentType = componentType;
		constructorSignature = constructorString;
		signChops = initSignatureChops(constructorSignature);
	}
	
	public StructuralMapping(Element mapping)
			throws InPUTException {
		this(mapping, mapping.getAttributeValue(Q.TYPE_ATTR), mapping.getAttributeValue(Q.CONSTR_ATTR));
	}

	private StructuralMapping(String id, StructuralMapping mapping, String componentType, String constructorSignature) {
		super(id, mapping.getSetter(), mapping.getGetter());
		complex = mapping.getComplex();
		this.componentType =  componentType;
		this.constructorSignature = constructorSignature;
		signChops = initSignatureChops(constructorSignature);
	}
	
	/**
	 * clone constructor
	 * @param id
	 * @param mapping
	 */
	public StructuralMapping(String id, StructuralMapping mapping) {
		this(id, mapping, mapping.getComponentType(), mapping.getConstructorSignature());
	}
	
	public boolean isComplex() {
		return complex != null;
	}

	public Complex getComplex() {
		return complex;
	}

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
	
	public String getConstructorSignature() {
		return constructorSignature; 
	}
	

	private static Set<String> initSignatureChops(String constructorSignature) {
		Set<String> chops;
		if (constructorSignature != null) {
			String[] entries = constructorSignature.split(Pattern.quote(" "));
			chops = new HashSet<String>(Arrays.asList(entries));
		} else
			chops = new HashSet<String>();
		return chops;
	}
	
	/**
	 * important note: dependability is only relevant for the global order of the 
	 * parameter init, and therefore, a parameter is NOT dependent on its children,
	 * meaning that local dependencies are not of interest here.
	 */
	public boolean containsInConstructorSignature(String paramId) {
		if (signChops.contains(paramId))
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return getId() + "[constructor=\"" + constructorSignature +"\"]";
	}

	public String getComponentType() {
		return componentType;
	}

	@Override
	public IMapping clone(String id, IMapping mapping) {
		if (mapping != null){
			StructuralMapping m = (StructuralMapping) mapping;
			String constructor = m.getConstructorSignature();
			if (constructor == null)
				constructor = constructorSignature;
			return new StructuralMapping(id, this, m.getComponentType(), constructor);
		}
		return new StructuralMapping(id, this);
	}

	//TODO reusing same code, could be packaged into one function setAttribute for Element.
	@Override
	public IMapping clone(String id, Element mergedPriority) throws InPUTException {
		String constructor = mergedPriority.getAttributeValue(Q.CONSTR_ATTR);
		if (constructor == null)
			constructor = constructorSignature;
		if (mergedPriority.getAttributeValue(Q.GET_ATTR) == null && getGetter() == null)
			mergedPriority.setAttribute(new Attribute(Q.GET_ATTR, "false"));
		if (mergedPriority.getAttributeValue(Q.SET_ATTR) == null && getSetter() == null)
			mergedPriority.setAttribute(new Attribute(Q.SET_ATTR, "false"));
		
		return new StructuralMapping(mergedPriority, componentType, constructor);
	}
}
