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
import java.util.List;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.SValue;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.generator.FixedStructuralGenerator;
import se.miun.itm.input.model.param.generator.StructuralGenerator;
import se.miun.itm.input.util.Q;

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
 * @NotThreadSafe
 */
public class SChoice extends AStruct {

	private static final long serialVersionUID = 6904132820484219539L;

	private final List<SParam> structChildren;

	public SChoice(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		((SParam)getParent()).addChildParam(this);
		structChildren = initStructChildren();
	}

	@Override
	protected StructuralGenerator initGenerator(boolean initRanges) throws InPUTException {
			return new FixedStructuralGenerator(this, getLocalId());
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

	public List<SParam> getStructChildren() {
		return structChildren;
	}

	protected Enum<?> getEnumValue() throws InPUTException {
		return generator.getEnumValue();
	}

	@Override
	public String getParamId() {
		return ((SParam)getParent()).getId();
	}

	@Override
	public boolean isImplicit() {
		return false;
	}

	protected String getComponentId() throws InPUTException {
		return generator.getComponentType();
	}

	@Override
	protected boolean isInitByConstructor(String localId) throws InPUTException {
		return getInPUTConstructor().isLocalInitByConstructor(localId)
				|| ((SParam)getParent()).getInPUTConstructor()
						.isLocalInitByConstructor(localId);
	}

	@Override
	public Object[] getStringTypeActualParam() {
		Object[] actualStringParams = { getLocalId() };
		return actualStringParams;
	}

	@Override
	public boolean isStringType() {
			return ((SParam)getParent()).isStringType();
	}
	
	@Override
	public Object getValueForString(String stringValue) throws InPUTException {
		return newInstance(null, null);
	}
	
	@Override
	public String getValueForIndex(int index) {
		return getLocalId();
	}
	
	@Override
	public String toString() {
		return getLocalId();
	}
	
	@Override
	public boolean isFixed() {
		return false;
	}

	@Override
	public AStruct getChoiceById(String localChoiceId) {
		return this;
	}

	public Object getFixedChoice(Object value) throws InPUTException {
			return generator.next(null);
	}

	@Override
	public boolean isComplex() throws InPUTException {
		return false;
	}

	@Override
	public void initIfNotConstructorInit(SValue sValue, Value<?> subValue, Object value) throws InPUTException {
		if (!isInitByConstructor(subValue.getLocalId()) && subValue.getParam().hasSetHandle())
			subValue.getParam().injectOnParent(subValue, value);
	}

	@Override
	public String getLocalChildIdByComponentId(String className) throws InPUTException {
		return getLocalId();
	}
	
	public void setFixed(String value) throws InPUTException {
		if (value != null) {
			setAttribute(Q.FIXED_ATTR, value);
		}else{
			removeAttribute(Q.FIXED_ATTR);
		}
		generator = initGenerator(true);
	}

	@Override
	public String getFixedValue() {
		return getLocalId();
	}
}