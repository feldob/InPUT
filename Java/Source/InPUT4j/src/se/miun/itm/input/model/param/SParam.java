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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private final List<SChoice> choices = new ArrayList<SChoice>();

	private final Map<String, Integer> choicesPositions = new HashMap<String, Integer>();
	
	private final Map<String, SChoice> choicesMap = new HashMap<String, SChoice>();

	private final Map<String, String> localChildIds = new HashMap<String, String>();

	private boolean init = false;

	private Map<String, String> localIdsByComponentId = new HashMap<String, String>();;

	public SParam(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		initToParent();
	}

	@Override
	protected StructuralGenerator initGenerator() throws InPUTException {
		String fixedValue = getAttributeValue(Q.FIXED_ATTR);
		if (fixedValue != null){
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

	@Override
	protected void addChildParam(Param<?> child) {
		super.addChildParam(child);
		localChildIds.put(child.getId(), child.getLocalId());
	}

	protected void addChoice(SChoice choice) {
		addChildParam(choice);
		choices.add(choice);
		choicesPositions.put(choice.getLocalId(), choices.size());
		choicesMap.put(choice.getLocalId(), choice);
	}

	public List<SChoice> getChoices() {
		return choices;
	}

	public String getLocalChildId(String paramId) {
		return localChildIds.get(paramId);
	}

	public String getLocalChildIdByComponentId(String id) throws InPUTException {
		if (!init)
			init();
		return localIdsByComponentId.get(id);
	}

	private void init() throws InPUTException {
		List<Element> children = getChildren();
		SChoice choice;
		for (Element child : children) {
			if (child instanceof SChoice) {
				choice = ((SChoice) child);
				localIdsByComponentId.put(choice.getComponentId(),
						choice.getLocalId());
			}
		}
		init = true;
	}

	public AStruct getChoiceById(String localChoiceId) {
		if (isImplicit())
			return this;
		return choicesMap.get(localChoiceId);
	}

	@Override
	public String getParamId() {
		return getId();
	}

	public AStruct nextChoice() {
		if (choices.size() > 0)
			return choices.get(ps.getRNG().nextInt(choices.size()));
		else
			return this;
	}

	@Override
	public boolean isImplicit() {
		return choices.isEmpty();
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

	public void reflectObject(SValue sValue, Object[] actualParams,
			AStruct choice) throws InPUTException {
		if (isEnum())
			sValue.setPlainInputValue(((SChoice) choice).getEnumValue());
		else
			sValue.setPlainInputValue(choice.newInstance(actualParams));
	}

	@Override
	public void init(Value<?> sValue, Object[] actualParams,
			ElementCache elementCache) throws InPUTException {
		if (isPlainValueElement(sValue))
			initValue(sValue, actualParams, elementCache);
		else if (generator.isComplex())
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

	public void initIfNotConstructorInit(SValue sValue,
			Value<? extends Param<?>> subValue, Object value)
			throws InPUTException {
		String localId = sValue.getAttributeValue(Q.VALUE_ATTR);
		AStruct choice = getChoiceById(localId);
		if (!choice.isInitByConstructor(subValue.getLocalId())
				&& subValue.getParam().hasSetHandle())
			subValue.getParam().injectOnParent(subValue, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initValue(Value<?> value, Object[] actualParams,
			ElementCache elementCache) throws InPUTException {

		List<Value<Param<?>>> children = (List<Value<Param<?>>>) (List<?>) value
				.getChildren();
		// set the subParams and all the peers that are of type numeric.
		List<Value<? extends Param<?>>> subParamValueElements = Arrays
				.asList(children.toArray(new Value<?>[0]));

		String localChoiceId = value.getAttributeValue(Q.VALUE_ATTR);
		AStruct choice = getChoiceById(localChoiceId);
		if (choice == null) {
			throw new InPUTException(value.getId()
					+ ": There is no choice element '" + localChoiceId
					+ "' for parameter '" + getId() + "'.");
		}

		// making available possible customizable input
		if (isImplicit() && localChoiceId != null
				&& !localChoiceId.equals(getLocalId()))
			actualParams = SParam.extendActualParams(actualParams,
					localChoiceId);

		actualParams = choice.enhanceActualParams(actualParams,
				subParamValueElements, elementCache);

		if (isStringType()) {
			Object valueString = value.getAttributeValue(Q.VALUE_ATTR);
			if (valueString == null)
				valueString = actualParams[0];
			value.setInputValue(valueString);
		} else
			reflectObject((SValue) value, actualParams, choice);

		for (Value<?> subValueE : subParamValueElements)
			// inject the child values into the members
			initIfNotConstructorInit((SValue) value, subValueE,
					value.getInputValue(null));

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

	public void appendAllChoicesToString(StringBuilder builder) {
		if (isImplicit()) {
			builder.append(getLocalId());
		} else {
			for (int i = 0; i < choices.size(); i++) {
				builder.append(choices.get(i).toString());
				if (i < choices.size() - 1) {
					builder.append(", ");
				}
			}
		}
	}

	@Override
	public boolean isFixed() {
		return super.isFixed() || (choices.size() < 2);
	}

	@Override
	public String getFixedValue() {
		String fixed = null;
		if (choices.isEmpty())
			fixed = getLocalId();
		else if (choices.size() < 2)
			fixed = choices.get(0).getLocalId();
		else
			fixed = super.getFixedValue();
		return fixed;
	}

	public boolean isComplex() throws InPUTException {
		return generator.isComplex();
	}

	public Integer getIndexForLocalChoiceId(String chop) {
		return choicesPositions.get(chop);
	}

	public Param<StructuralGenerator> getChoiceByPosition(int index) {
		return choices.get(index-1);
	}
}