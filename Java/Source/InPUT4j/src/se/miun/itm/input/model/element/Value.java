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

import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * Values are generic containers for parameter values, that are abstractly defined by a private "value" fields of type object. Each value is
 * defined with respect to one specific parameter. That parameter restricts the range of the value.
 * 
 * A value element is responsible to collect the necessary context, based on parameter, element cache etc. in order to instantiate
 * semantically correct instances of the parameter type, considering all user defined meta information. This is done using reflection.
 * 
 * A value can not only be of simple value type, but as well of array or customizable collection type, which makes it very flexible for use.
 * Both, structural and numerical values can be of arbitrary array sizes.
 * 
 * The type can be specified using the xml structures.
 * 
 * @author Felix Dobslaw
 * 
 * @param <AParam>
 * @NotThreadSafe
 */
public abstract class Value<AParam extends Param<?>> extends InPUTElement {

	private static final long serialVersionUID = -8840060757041893991L;

	private Object value;

	protected final AParam param;

	protected final int[] dimensions;

	protected final ElementCache elementCache;

	public Value(String name, AParam param, Element original, int[] sizeArray, ElementCache elementCache) throws InPUTException {
		super(name, Q.DESIGN_NAMESPACE, param, original);

		this.param = param;
		dimensions = ParamUtil.initDimensions(sizeArray);
		initLocalId(original);

		this.elementCache = elementCache;
	}

	private void initLocalId(Element original) {
		String localId;
		if (original != null && ParamUtil.isIntegerString(original.getAttributeValue(Q.ID_ATTR))) {
			localId = original.getAttributeValue(Q.ID_ATTR);
		} else {
			if (param instanceof SChoice) {
				localId = param.getParentElement().getAttributeValue(Q.ID_ATTR);
			} else {
				localId = param.getAttributeValue(Q.ID_ATTR);
			}
		}
		setAttribute(Q.ID_ATTR, localId);
	}

	public Value(String name, AParam param, int[] sizeArray, ElementCache elementCache) throws InPUTException {
		this(name, param, null, sizeArray, elementCache);
	}

	@Override
	public Element addContent(Content childValueElement) {
		renewLocalId(childValueElement);
		return super.addContent(childValueElement);
	}

	private void renewLocalId(Content childContent) {
		if (childContent instanceof Value<?>) {
			Value<?> childValueElement = (Value<?>) childContent;
			String newFullChildId;
			String childLocalId = childValueElement.getAttributeValue(Q.ID_ATTR);
			Element childParameterParent = childValueElement.getParam().getParentElement();
			if (childParameterParent instanceof Param && !childParameterParent.equals(param) && getAttributeValue(Q.VALUE_ATTR) != null) {
				newFullChildId = getId() + "." + getAttributeValue(Q.VALUE_ATTR) + "." + childLocalId;
			} else
				newFullChildId = getId() + "." + childLocalId;
			childValueElement.setFullId(newFullChildId);
		}
	}

	/**
	 * uses lazy loading.
	 * 
	 * @param actualParams
	 * @return
	 * @throws InPUTException
	 */
	public Object getInputValue(Object[] actualParams) throws InPUTException {
		if (value == null)
			param.init(this, actualParams, elementCache);
		return value;
	}

	public boolean isArrayType() {
		return Param.isArrayType(dimensions);
	}

	public String valueToString() {
		String result;
		if (isArrayType())
			result = arrayStringValue();
		else
			result = getAttributeValue(Q.VALUE_ATTR);
		return result;
	}

	private String arrayStringValue() {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < dimensions.length; i++) {
			b.append('[');
			b.append(dimensions[i]);
			b.append(']');
		}
		return b.toString();
	}

	public void setInputValue(Object value) throws InPUTException {
		this.value = value;
	}

	@Override
	public String toString() {
		return "[" + super.toString() + ", value = " + valueToString() + "]";
	}

	protected abstract void initRandom(Map<String, Object> vars, Object[] actualParams, boolean lazy) throws InPUTException;

	public Param<?> getParam() {
		return param;
	}

	public String getLocalId() {
		return getAttributeValue(Q.ID_ATTR);
	}

	public Object getInputValue() {
		return value;
	}

	public Integer getDimensions() {
		return dimensions.length;
	}

	public void renewId() {
		if (param.isArrayType())
			setFullId(ParamUtil.deriveInputParamId(param.getParamStore(), this));
		for (Element child : getChildren())
			((Value<?>) child).renewId();
	}

	@SuppressWarnings("unchecked")
	public boolean same(Object obj) {
		if (!(obj instanceof Value))
			return false;

		Value<? extends Param<?>> foreigner = (Value<?>) obj;
		Attribute forAttr;
		for (Attribute attr : getAttributes()) {
			forAttr = foreigner.getAttribute(attr.getName());
			if (forAttr == null || !forAttr.getValue().equals(attr.getValue()))
				return false;
		}

		List<Value<?>> myChildren = (List<Value<?>>) (List<?>) getChildren();
		List<Value<?>> foreignerChildren = (List<Value<?>>) (List<?>) foreigner.getChildren();
		if (myChildren.size() != foreignerChildren.size())
			return false;

		Value<?> foreignerValue;
		for (Value<?> value : myChildren) {
			foreignerValue = getSame(value, foreignerChildren);
			if (foreignerValue == null || !value.same(foreignerValue))
				return false;
		}

		return true;
	}

	private Value<?> getSame(Value<?> value, List<Value<?>> foreignerChildren) {
		for (Value<?> foreignerValue : foreignerChildren)
			if (value.getId().equals(foreignerValue.getId()))
				return foreignerValue;
		return null;
	}

	public boolean isPlainType() {
		return dimensions.length == 1 && dimensions[0] == 0;
	}

	public boolean isParentInitialized() {
		Element parent = getParentElement();
		if (parent != null && parent instanceof Value<?>) {
			Value<?> parentValue = (Value<?>) parent;
			return parentValue.getInputValue() != null;
		}
		return true;
	}

}