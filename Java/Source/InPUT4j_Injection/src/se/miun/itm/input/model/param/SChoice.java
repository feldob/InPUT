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

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;

/**
 * A structural choice element can in principle be seen as a new root to a new
 * parameter space definition, as it represents a choice for its parameter,
 * possibly introducing multiple new sub-parameters.
 * 
 * structural choices have to always be of their SPAram supertype, with the
 * exception of Complex mappings, where the mapping can be chosen to be of
 * another sub-type.
 * 
 * @author Felix Dobslaw
 * 
 */
public class SChoice extends AStruct {

	private static final long serialVersionUID = 6904132820484219539L;

	private final List<SParam> structChildren;

	private final Enum<?> enumValue;

	final String paramId;

	public SChoice(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		SParam param = ((SParam) getParentElement());
		param.addChoice(this);
		paramId = param.getId();
		structChildren = initStructChildren();
		enumValue = initEnum();
		initInPUTConstructor();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Enum<?> initEnum() {
		if (isEnum())
			return Enum.valueOf((Class<? extends Enum>) getSuperClass(),
					getLocalId());
		return null;
	}

	private List<SParam> initStructChildren() {
		List<SParam> structChildren = new ArrayList<SParam>();
		SParam structChild;
		for (Object childElement : getChildren())
			if (childElement instanceof SParam) {
				structChild = (SParam) childElement;
				structChildren.add(structChild);
				addChildParam(structChild);
			}
		return structChildren;
	}

	@Override
	protected boolean initIsEnum() {
		return ((SParam) getParentElement()).isEnum();
	}

	@Override
	protected Class<?> initSuperClass() {
		return ((SParam) getParentElement()).getSuperClass();
	}

	public List<SParam> getStructChildren() {
		return structChildren;
	}

	public Enum<?> getEnumValue() {
		return enumValue;
	}

	@Override
	public String getParamId() {
		return paramId;
	}

	@Override
	public Object getFixedValue() {
		return this;
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

	public String getComponentId() {
		return mapping.getComponentId();
	}

	@Override
	public boolean isComplex() {
		return ((SParam) getParentElement()).isComplex();
	}
}