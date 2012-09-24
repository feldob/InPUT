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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.mapping.EmptyMapping;

/**
 * The concrete implementation of complex parameter meta definitions, being an
 * extended wrapper to the SParam entry in DesignSpace.xsl. SParams can either
 * be of user customizable type, such as String, without specific user choices,
 * or with a set of choices that can be selected as alternatives for an instance
 * of a design space.
 * 
 * @author Felix Dobslaw
 * 
 */
public class SParam extends AStruct {

	private static final long serialVersionUID = -4899687772776177806L;

	private final List<SChoice> choices = new ArrayList<SChoice>();

	private final Map<String, SChoice> choicesMap = new HashMap<String, SChoice>();

	private final Map<String, String> localChildIds = new HashMap<String, String>();

	private boolean init = false;

	private Map<String, String> localIdsByComponentId = new HashMap<String, String>();;

	public SParam(Element original, String designId, ParamStore ps)
			throws InPUTException {
		super(original, designId, ps);
		initToParent();
		initInPUTConstructor();
	}

	// could have been missed by the init, because of the order!
	private void initToParent() {
		if (getParentElement() instanceof SParam) {
			((SParam) getParentElement()).addChildParam(this);
		}
	}

	@Override
	protected void addChildParam(Param child) {
		super.addChildParam(child);
		localChildIds.put(child.getId(), child.getLocalId());
	}

	protected void addChoice(SChoice choice) {
		addChildParam(choice);
		choices.add(choice);
		choicesMap.put(choice.getLocalId(), choice);
	}

	protected Class<?> initSuperClass() throws InPUTException {
		try {
			String mappingId = mapping.getComponentId();
			if (mappingId == null) {
				// log.warn(": No code mapping has been defined for the structural parameter '"
				// + getId() + "'.");
				return null;
			}
			return Class.forName(mappingId);
		} catch (ClassNotFoundException e) {
			throw new InPUTException(getDesignSpaceId()
					+ ": There is no such class '" + mapping.getComponentId()
					+ "' that has been defined for structural parameter '"
					+ getId() + "'.", e);
		}
	}

	@Override
	protected boolean initIsEnum() throws InPUTException {
		if (!(mapping instanceof EmptyMapping)) {
			try {
				return Class.forName(mapping.getComponentId()).isEnum();
			} catch (ClassNotFoundException e) {
				throw new InPUTException(getId() + ": There is no such class '"
						+ mapping.getComponentId() + "'.", e);
			}
		}
		return false;
	}

	public List<SChoice> getChoices() {
		return choices;
	}

	public String getLocalChildId(String paramId) {
		return localChildIds.get(paramId);
	}

	public String getLocalChildIdByComponentId(String id) {
		if (!init)
			init();
		return localIdsByComponentId.get(id);
	}

	@SuppressWarnings("unchecked")
	private void init() {
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
	public Object getFixedValue() {
		return getChoiceById(getFixed());
	}

	@Override
	public boolean isImplicit() {
		return choices.isEmpty();
	}

	@Override
	public boolean isComplex() {
		return mapping.isComplex();
	}
}