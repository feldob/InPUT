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

import java.util.Arrays;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.SValue;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.generator.FixedStructuralGenerator;
import se.miun.itm.input.model.param.generator.RandomStructuralGenerator;
import se.miun.itm.input.model.param.generator.StructuralGenerator;
import se.miun.itm.input.util.Q;

/**
 * The concrete implementation of complex parameter meta definitions, being an
 * extended wrapper to the SParam entry in DesignSpace.xsl. SParams can either
 * be of user customizable type, such as String, without specific user choices,
 * or with a set of choices that can be selected as alternatives for an instance
 * of a design space.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class SParam extends AStruct {

	private static final long serialVersionUID = -4899687772776177806L;

	public SParam(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		initToParent();
	}

	@Override
	protected StructuralGenerator initGenerator(boolean initRanges)
			throws InPUTException {
		String fixedValue = getAttributeValue(Q.FIXED_ATTR);
		if (fixedValue != null) {
			return new FixedStructuralGenerator(this, fixedValue);
		}
		return new RandomStructuralGenerator(this, ps.getRNG());
	}

	// could have been missed by the init, because of the order!
	private void initToParent() {
		if (getParentElement() instanceof SParam) {
			((SParam) getParentElement()).addChildParam(this);
		}
	}

	// assumes that each child is bijectively mapped to a component!
	public String getLocalChildIdByComponentId(String id) throws InPUTException {
		SChoice choice;
		for (Element child : getChildren()) {
			if (child instanceof SChoice) {
				choice = ((SChoice) child);
				if (choice.getComponentId().equals(id))
					return choice.getLocalId();
			}
		}
		return null;
	}

	@Override
	public AStruct getChoiceById(String localChoiceId) {
		if (isImplicit())
			return this;

		for (Element child : getChildren())
			if (child.getAttributeValue(Q.ID_ATTR).equals(localChoiceId))
				return (AStruct) child;
		return null;
	}

	@Override
	public String getParamId() {
		return getId();
	}

	public AStruct nextChoice() throws InPUTException {
		int choices = getAmountChoices();
		if (choices > 0)
			return getChoiceByPosition(ps.getRNG().nextInt(choices) + 1);
		else
			return this;
	}

	@Override
	public boolean isImplicit() {
		return getAmountChoices() == 0;
	}

	@Override
	public Object[] getStringTypeActualParam() {
		Object[] actualStringParams = { Q.DEFAULT };
		return actualStringParams;
	}

	@Override
	public boolean isStringType() {
		String type = getAttributeValue(Q.TYPE_ATTR);
		return type != null
				&& (type.equals(Q.STRING) || type.startsWith(Q.STRING)
						&& type.contains("["));
	}

	@Override
	public void init(Value<?> sValue, Object[] actualParams,
			ElementCache elementCache) throws InPUTException {
		if (isPlainValueElement(sValue)
				&& !(isComplex() && sValue.getAttributeValue(Q.VALUE_ATTR) == null))
			initValue(sValue, actualParams, elementCache);
		else if (isComplex())
			((SValue) sValue).setPlainInputValue(initComplex(sValue,
					actualParams));
		else if (isArrayType())
			initArray(sValue, actualParams);
	}

	public static Object[] extendActualParams(Object[] actualParams,
			String localChoiceId) {
		Object[] newParams;
		if (actualParams != null) {
			newParams = Arrays.copyOf(actualParams, actualParams.length);
		} else {
			newParams = new Object[1];
		}

		newParams[newParams.length - 1] = localChoiceId;
		return newParams;
	}

	@Override
	public void initIfNotConstructorInit(SValue sValue, Value<?> subValue,
			Object value) throws InPUTException {
		String localId = sValue.getAttributeValue(Q.VALUE_ATTR);
		AStruct choice = getChoiceById(localId);
		if (!choice.isInitByConstructor(subValue.getLocalId())
				&& subValue.getParam().hasSetHandle())
			subValue.getParam().injectOnParent(subValue, value);
	}

	@Override
	public Object getValueForString(String valueString) throws InPUTException {
		if (isImplicit())
			valueString = getId();
		return generator.parse(valueString);
	}

	@Override
	public String getValueForIndex(int index) throws InPUTException {
		return generator.getValueForIndex(index);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		appendAllChoicesToString(builder);
		builder.append('}');
		return builder.toString();
	}

	private void appendAllChoicesToString(StringBuilder builder) {
		if (isImplicit()) {
			builder.append(getLocalId());
		} else {
			int choices = getAmountChoices();
			for (int i = 0; i < choices; i++) {
				try {
					builder.append(getChoiceByPosition(i + 1).toString());
				} catch (InPUTException e) {
					e.printStackTrace();
				}
				if (i < choices - 1)
					builder.append(", ");
			}
		}
	}

	@Override
	public boolean isFixed() {
		int choices = getAmountChoices();
		return getAttributeValue(Q.FIXED_ATTR) != null || choices <= 1;
	}

	@Override
	public String getFixedValue() throws InPUTException {
		int choices = getAmountChoices();
		if (getAmountChoices() == 0)
			return getLocalId();
		else if (choices < 2)
			return getChoiceByPosition(1).getLocalId();
		else
			return getAttributeValue(Q.FIXED_ATTR);
	}

	public boolean isComplex() throws InPUTException {
		return generator.isComplex();
	}

	public AStruct getChoiceByPosition(int index) throws InPUTException {
		int counter = 1;
		for (Element child : getChildren())
			if (child instanceof SChoice) {
				if (counter == index) {
					return (AStruct) child;
				}
				counter++;
			}
		throw new InPUTException("The index " + index
				+ " is not valid for parameter '" + getId() + "'.");
	}

	public Object getFixedChoice(Object value) throws InPUTException {
		if (!value.getClass().isArray())
			return generator.next(null);
		return value;
	}

	public void setFixed(String value) throws InPUTException {
		if (value == null)
			removeAttribute(Q.FIXED_ATTR);
		else if (getChoiceById(value) != null)
			setAttribute(Q.FIXED_ATTR, value);
		else
			throw new InPUTException("The passed value '" + value
					+ "' is of wrong type for parameter '" + getId() + "'.");
		generator = initGenerator(true);
	}

	public int getAmountChoices() {
		int counter = 0;
		for (Element child : getChildren())
			if (child instanceof SChoice)
				counter++;
		return counter;
	}
}