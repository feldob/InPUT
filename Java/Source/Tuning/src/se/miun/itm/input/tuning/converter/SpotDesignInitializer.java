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
 */package se.miun.itm.input.tuning.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.ByteArrayExporter;
import se.miun.itm.input.impOrt.InputStreamImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.tuning.sequential.spot.SpotDesign;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.SAXUtil;

public class SpotDesignInitializer {

	private static final String QUOTE = Pattern.quote(".");

	private XMLOutputter outputter;

	private InputStreamImporter importer;

	public SpotDesignInitializer() throws InPUTException {
		outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		importer = new InputStreamImporter();
	}

	private String getValueString(Param<?> param, String valueString) throws InPUTException {
		if (param instanceof SParam) {
			if (valueString != null && ((SParam) param).getChoiceById(valueString) == null) {
				try {
					int index = new BigDecimal(valueString).intValue();// ensure int
					valueString = ((AStruct) param).getValueForIndex(index);
				} catch (NumberFormatException e) {
					System.out.println("test");
				}
			}
		} else if (param instanceof NParam)
			if (((NParam) param).isBoolean() && !isBoolean(valueString))
				valueString = new Boolean(new BigDecimal(valueString).compareTo(BigDecimal.ZERO) > 0).toString();
		return valueString;
	}

	private boolean isBoolean(String valueString) {
		return valueString.equals("true") || valueString.equals("false");
	}

	private IDesign initDesign(int designId, Map<Integer, Set<ParamValuePair>> values, IDesignSpace space) throws InPUTException {
		if (space == null)
			return null;

		ParamStore store = ParamStore.getInstance(space.getId());

		IDesign inputDesign = space.nextEmptyDesign("" + designId);

		Document designTemplate = extractEmptyXMLTemplateFor(inputDesign);

		fillDesignTemplateWithValues(designTemplate, values, space, store);

		return reinitiateDesignTemplate(inputDesign, designTemplate);
	}

	private void fillDesignTemplateWithValues(Document designTemplate, Map<Integer, Set<ParamValuePair>> values, IDesignSpace space,
			ParamStore store) throws InPUTException {
		int max = values.keySet().size();

		Element root = designTemplate.getRootElement();
		Set<ParamValuePair> pairs;
		for (int i = 1; i <= max; i++) {
			pairs = values.get(i);
			for (ParamValuePair pair : pairs) {
				if (belongsToDesignSpace(store, pair)) {
					addPairToRoot(root, pair, space, store);
				}
			}
		}
	}

	public boolean belongsToDesignSpace(ParamStore store, ParamValuePair pair) {

		String processedParamId = makeParamId(pair.paramId);

		return store.containsParam(processedParamId);
	}

	private String makeParamId(String paramId) {
		String[] chops = paramId.split(QUOTE);

		int pointer = chops.length - 1;
		int i = 0;
		boolean foundInt = true;
		while (foundInt) {
			if (pointer < 0)
				throw new IllegalArgumentException("The Input parameter id \"" + paramId + "\" is not valid.");

			try {
				Integer.parseInt(chops[pointer]);
				i++;
				pointer--;
			} catch (NumberFormatException e) {
				foundInt = false;
			}
		}

		paramId = reassembleChops(paramId, chops, i);

		return paramId;
	}

	private String reassembleChops(String paramId, String[] chops, int indexedPositions) {
		if (indexedPositions == 0)
			return paramId;

		chops = Arrays.copyOf(chops, chops.length - indexedPositions);

		StringBuilder b = new StringBuilder();

		for (int i = 0; i < chops.length - 1; i++) {
			b.append(chops[i]);
			b.append(".");
		}
		b.append(chops[chops.length - 1]);
		return b.toString();
	}

	private void addPairToRoot(Element root, ParamValuePair pair, IDesignSpace space, ParamStore store) throws InPUTException {

		String realParamId = makeParamId(pair.paramId);
		// 1) get the parameter for the id
		Param<?> param = store.getParam(realParamId);
		// 2) find out the parent it would have in a real setup. -> get id.
		Element parent = findParent(root, param, pair, store, space);
		// 3) add the tuple to the parent
		if (isAppropriateSubParam(parent, param)) {
			addToParent(pair, param, parent);
		}
	}

	public Element addToParent(ParamValuePair pair, Param<?> param, Element parent) throws InPUTException {
		Element result = containsAsChild(parent, ParamUtil.deriveLocalId(pair.paramId));
		if (result == null) {
			String valueString = getValueString(param, pair.value);
			Element valueElement = createValueElement(pair, param, valueString);
			parent.addContent(valueElement);
			result = valueElement;
		}
		return result;
	}

	private boolean isAppropriateSubParam(Element parent, Param<?> param) {
		if (parent == null || containsAsChild(parent, param.getLocalId()) != null)
			return false;

		if (parent.isRootElement())
			return true;

		String parentValue = parent.getAttributeValue(Q.VALUE_ATTR);
		String parentLocalId = parent.getAttributeValue(Q.ID_ATTR);
		if (parentValue == null)
			return true;

		// param.paramId second last entry must match "value" attribute of
		// parent!
		String paramParentLocalId = ((Param<?>) param.getParentElement()).getLocalId();
		if (paramParentLocalId.equals(parentValue) || paramParentLocalId.equals(parentLocalId))
			return true;

		return false;
	}

	private Element containsAsChild(Element parent, String paramLocalId) {
		for (Element child : parent.getChildren()) {
			if (child.getAttributeValue(Q.ID_ATTR).equals(paramLocalId))
				return child;
		}
		return null;
	}

	private Element findParent(Element root, Param<?> param, ParamValuePair pair, ParamStore store, IDesignSpace space)
			throws InPUTException {

		Element resultParent = null;
		if (isComplex(param))
			resultParent = findParentForComplex(root, param, pair, store, space);
		else if (hasComplexGrandParent(param))
			addPairToAllSuitableParentsForGrandChildOfComplex(root, param, pair, store, space);
		else
			resultParent = findParentForNormal(root, param, pair, store, space);

		return resultParent;

	}

	private void addPairToAllSuitableParentsForGrandChildOfComplex(Element root, Param<?> param, ParamValuePair pair, ParamStore store,
			IDesignSpace space) throws InPUTException {
		SChoice parent = (SChoice) param.getParentElement();
		// store the localId that has to match the value for the choices.
		String parentValueFilter = parent.getLocalId();
		// find the complex
		Element complexElement = findParent(root, parent, new ParamValuePair(parent.getId(), null), store, space);
		for (Element choice : complexElement.getChildren()) {
			if (choice.getAttributeValue(Q.VALUE_ATTR).equals(parentValueFilter)) {
				addToParent(pair, param, choice);
			}
		}

	}

	private boolean hasComplexGrandParent(Param<?> param) throws InPUTException {
		Element grandParent = param.getParentElement().getParentElement();
		return grandParent != null && !grandParent.isRootElement() && isComplex((Param<?>) grandParent);
	}

	public boolean isComplex(Param<?> param) throws InPUTException {
		return param instanceof SParam && ((SParam) param).isComplex();
	}

	public Element findParentForComplex(Element root, Param<?> param, ParamValuePair pair, ParamStore store, IDesignSpace space)
			throws InPUTException {
		Element parent = null;
		if (param.getParentElement().isRootElement()) {
			parent = root;
		} else {
			parent = findParentForNormal(root, param, pair, store, space);
		}

		parent = addToParent(new ParamValuePair(param.getId(), null), param, parent);

		return parent;
	}

	public Element findParentForNormal(Element root, Param<?> param, ParamValuePair pair, ParamStore store, IDesignSpace space)
			throws InPUTException {
		if (param.getParentElement().isRootElement())
			return root;

		Element resultParent = null;
		String parentId = ParamUtil.getParentId(param);
		Element superParent = root;
		String potentialParentId;

		for (Element potentialParent : superParent.getChildren()) {
			potentialParentId = ParamUtil.deriveInputParamId(store, potentialParent);
			if (parentId.equals(potentialParentId)) {
				resultParent = potentialParent;
				break;
			}

			if (parentId.startsWith(potentialParentId + ".")) {
				resultParent = findParent(potentialParent, param, pair, store, space);
			}
		}

		return resultParent;
	}

	private IDesign reinitiateDesignTemplate(IDesign inputDesign, Document designTemplate) throws InPUTException {
		String output = outputter.outputString(designTemplate);
		importer.resetContext(new ByteArrayInputStream(output.getBytes()), InPUTConfig.isValidationActive());
		inputDesign.impOrt(importer);

		return inputDesign;
	}

	private Element createValueElement(ParamValuePair pair, Param<?> param, String valueString) throws InPUTException {
		Element valueElement = new Element(param.getValueTypeString(), Q.DESIGN_NAMESPACE);
		setAppropriateId(pair, param, valueElement);
		if (valueString != null && !valueString.equals(""))
			valueElement.setAttribute(Q.VALUE_ATTR, valueString);
		return valueElement;
	}

	private void setAppropriateId(ParamValuePair pair, Param<?> param, Element valueElement) {
		boolean isInt = false;
		String[] chops = pair.paramId.split(QUOTE);
		try {
			Integer.parseInt(chops[chops.length - 1]);
			isInt = true;
		} catch (Exception e) {
		}
		String valueString = param.getLocalId();
		if (isInt)
			valueString = chops[chops.length - 1];

		valueElement.setAttribute(Q.ID_ATTR, valueString);
	}

	private Document extractEmptyXMLTemplateFor(IDesign design) throws InPUTException {
		ByteArrayOutputStream designStream = design.export(new ByteArrayExporter());
		Document jdomDesign = SAXUtil.build(new ByteArrayInputStream(designStream.toByteArray()), false);
		return jdomDesign;
	}

	public IExperiment initExperiment(int experimentId, List<Param<?>> fixed, SpotDesign spotDesign, IInPUT input) throws InPUTException {
		IExperiment experiment = new Experiment("" + experimentId, input);

		Map<Integer, Set<ParamValuePair>> values = initValues(fixed, spotDesign);

		IDesign aDesign, pDesign;
		aDesign = initDesign(experimentId, values, input.getAlgorithmDesignSpace());
		pDesign = initDesign(experimentId, values, input.getPropertySpace());
		experiment.setPreferences(pDesign);
		experiment.setAlgorithmDesign(aDesign);
		return experiment;
	}

	private Map<Integer, Set<ParamValuePair>> initValues(List<Param<?>> fixed, SpotDesign spotDesign) throws InPUTException {
		Map<Integer, Set<ParamValuePair>> paramMap = new HashMap<Integer, Set<ParamValuePair>>();

		addAllVariableEntries(paramMap, spotDesign);
		addAllFixedEntries(paramMap, fixed);

		return paramMap;
	}

	private void addAllVariableEntries(Map<Integer, Set<ParamValuePair>> paramMap, SpotDesign spotDesign) {
		String[] paramIds = spotDesign.getParamIds();
		int depth;
		Set<ParamValuePair> setOfDepth;
		for (String paramId : paramIds) {
			depth = paramId.split(SpotConverter.ID_CHOP_PATTERN).length;
			setOfDepth = getSetOfDepth(depth, paramMap);
			setOfDepth.add(new ParamValuePair(paramId, spotDesign.getValueFor(paramId)));
		}
	}

	private void addAllFixedEntries(Map<Integer, Set<ParamValuePair>> paramMap, List<Param<?>> fixed) throws InPUTException {
		int depth;
		Set<ParamValuePair> setOfDepth;
		for (Param<?> param : fixed) {
			depth = param.getId().split(SpotConverter.ID_CHOP_PATTERN).length;
			if (param instanceof SParam && ((SParam) param).isComplex()) {
				setOfDepth = getSetOfDepth(depth+1, paramMap);
				addAllFixedEntriesForComplexParam(setOfDepth, param.getId(), param.getFixedValue().split(" "));
			} else {
				setOfDepth = getSetOfDepth(depth, paramMap);
				setOfDepth.add(new ParamValuePair(param.getId(), param.getFixedValue()));
			}
		}
	}

	private void addAllFixedEntriesForComplexParam(Set<ParamValuePair> setOfDepth, String paramId, String[] fixedValues) {
		for (int i = 0; i < fixedValues.length; i++) {
			if (!fixedValues[i].equals(Q.PLACEHOLDER)) {
				setOfDepth.add(new ParamValuePair(paramId + "." + (i + 1), fixedValues[i]));
			}
		}
	}

	private Set<ParamValuePair> getSetOfDepth(int depth, Map<Integer, Set<ParamValuePair>> paramMap) {
		Set<ParamValuePair> pairs;
		if (!paramMap.containsKey(depth)) {
			pairs = new HashSet<ParamValuePair>();
			paramMap.put(depth, pairs);
		} else
			pairs = paramMap.get(depth);
		return pairs;
	}
}