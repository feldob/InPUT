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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jdom.Element;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.eval.ParamEvaluationOrderComparator;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.IMappings;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.util.ParamInitializer;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.XPathProcessor;

/**
 * a The datastructure that manages all parameter related activities in InPUT.
 * Every design space has a parameter store, to which it outsources its
 * initialization, parameter lookup, and data consistency control.
 * 
 * The ParamStore makes sure that the mappings are bound correctly to the
 * parameter definitions, and else report otherwise.
 * 
 * @author Felix Dobslaw
 */
public class ParamStore implements Identifiable {

	private static final Map<String, ParamStore> stores = new HashMap<String, ParamStore>();

	private final Map<String, Param> params = new HashMap<String, Param>();

	private final Map<Param, String> paramsInv = new HashMap<Param, String>();

	private final String id;

	private final IMappings mappings;

	private final Document spaceTree;

	private final IDesignSpace space;

	private final Random rng;

	private final int hash;

	private ParamStore(IDesignSpace space, Document designSpace,
			InputStream mappingStream) throws InPUTException {
		id = designSpace.getRootElement().getAttributeValue(Q.ID_ATTR);
		hash = id.hashCode();
		this.space = space;
		this.spaceTree = designSpace;
		this.mappings = initCodeMapping(mappingStream);
		rng = initRNG(id);
		initParams();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	private IMappings initCodeMapping(InputStream mappingStream)
			throws InPUTException {
		String mappingFilePath = spaceTree.getRootElement().getAttributeValue(
				Q.MAPPING_ATTR);
		IMappings codeMappings = initCodeMappings(mappingFilePath,
				mappingStream);
		return codeMappings;
	}

	private IMappings initCodeMappings(String mappingFilePath,
			InputStream mappingStream) throws InPUTException {
		InputStream is = null;
		if (mappingStream != null)
			is = mappingStream;
		else if (mappingFilePath != null) {
			IMappings cm = Mappings.getInstance(id);
			if (cm != null) {
				return cm;
			}
			try {
				is = new FileInputStream(mappingFilePath);
			} catch (FileNotFoundException e1) {
				throw new InPUTException("The file by name '" + mappingFilePath
						+ "' could not be found.", e1);
			}
		}

		if (is != null) {
			String mappingId;
			try {
				mappingId = Mappings.initMapping(is);
			} catch (IOException e) {
				throw new InPUTException(
						"The inputStream for the codeMapping file could not be read successflly.",
						e);
			}

			if (mappingId != null) {
				Mappings.register(id, mappingId);
				return Mappings.getInstance(id);
			}
		}

		return null;
	}

	private Random initRNG(String designSpaceId) throws InPUTException {
		Random rng;
		if (designSpaceId.equals(Q.CONFIG_ID))
			rng = null;
		else
			rng = InPUTConfig.getValue(Q.RANDOM);
		return rng;
	}

	private void initParams() throws InPUTException {
		preprocessTreeForTypes();
		LinkedList<Element> params = ParamInitializer.getOrderedInitParams(
				spaceTree, mappings);

		for (Element param : params)
			initParam(id, (Element) param);

		initDependencies();
		initRanges();
	}

	@SuppressWarnings("unchecked")
	private void preprocessTreeForTypes() throws InPUTException {
		Element root = spaceTree.getRootElement();
		List<Element> params = root.getChildren();
		int i = 0;
		String typeId;
		while (i < params.size()
				&& params.get(i).getName().equals(Q.SCHOICE_TYPE)) {
			typeId = params.get(i).getAttributeValue(Q.ID_ATTR);

			// find all choices that refer to the type
			List<Object> dependers = XPathProcessor.query(
					createTypeQuery(typeId), Q.DESIGN_SPACE_NAMESPACE, root);

			for (Object choice : dependers)
				initChoiceByType((Element) choice, params.get(i));

			root.removeContent(params.get(i));
		}
	}

	private void initChoiceByType(Element choice, Element type) {
		Element newChoice = (Element) type.clone();
		String localId = choice.getAttributeValue(Q.ID_ATTR);
		addAlias(choice, type);
		newChoice.setAttribute(Q.ID_ATTR, localId);
		newChoice.setName(Q.SCHOICE);
		Element parent = choice.getParentElement();
		parent.removeContent(choice);
		parent.addContent(newChoice);
	}

	@SuppressWarnings("unchecked")
	private void addAlias(Element choice, Element newChoice) {
		String paramId = ParamUtil.deriveParamId(choice);
		String typeId = newChoice.getAttributeValue(Q.ID_ATTR);
		List<Element> children = newChoice.getChildren();
		for (Element typeChild : children)
			addSubAlias(paramId, typeId, typeChild);
		mappings.addAlias(paramId, typeId);
	}

	@SuppressWarnings("unchecked")
	private void addSubAlias(String paramId, String typeId, Element typeChild) {
		String localId = typeChild.getAttributeValue(Q.ID_ATTR);
		String newParamId = paramId + "." + localId;
		String newTypeId = typeId + "." + localId;
		List<Element> children = typeChild.getChildren();
		for (Element child : children)
			addSubAlias(newParamId, newTypeId, child);

		mappings.addAlias(newParamId, newTypeId);
	}

	// SChoice[@type='id']
	private String createTypeQuery(String typeId) {
		StringBuilder b = new StringBuilder();
		b.append("//");
		b.append(Q.SCHOICE);
		b.append("[@");
		b.append(Q.TYPE_ATTR);
		b.append("='");
		b.append(typeId);
		b.append("']");
		return b.toString();
	}

	private void initRanges() throws InPUTException {
		for (Param param : params.values())
			if (param instanceof NParam)
				((NParam) param).initRanges();
	}

	private void initParam(String id, Element param) throws InPUTException {
		Param paramE = null;
		if (param.getName().equals(Q.NPARAM))
			paramE = new NParam(param, id, this);
		else if (param.getName().equals(Q.SPARAM))
			paramE = new SParam(param, id, this);
		else if (param.getName().equals(Q.SCHOICE))
			paramE = new SChoice(param, id, this);
		initParam(paramE);
	}

	private void initDependencies() {
		List<Param> paramList;
		paramList = new ArrayList<Param>(params.values());

		Param first;
		for (int i = 0; i < paramList.size(); i++) {
			first = paramList.get(i);
			for (int j = i + 1; j < paramList.size(); j++)
				ParamEvaluationOrderComparator.init(first, paramList.get(j));
		}
	}

	private void initParam(final Param param) {
		String paramId = param.getId();
		params.put(paramId, param);
		paramsInv.put(param, paramId);

		// log.info(paramId + ": Success");
	}

	public boolean containsParam(String paramId) {
		return params.containsKey(paramId);
	}

	public Param getParam(String paramId) {
		return params.get(paramId);
	}

	public String getId() {
		return id;
	}

	public Collection<Param> getImplementationSpecific() {
		return Collections.unmodifiableCollection(paramsInv.keySet());
	}

	public Set<String> getParamIds() {
		return Collections.unmodifiableSet(params.keySet());
	}

	public static ParamStore getInstance(String id) {
		return stores.get(id);
	}

	public static Set<String> getInstanceIds() {
		return Collections.unmodifiableSet(stores.keySet());
	}

	public static void register(IDesignSpace space, InputStream mappingStream,
			Document designSpace) throws InPUTException {
		String id = designSpace.getRootElement().getAttributeValue(Q.ID_ATTR);
		if (!stores.containsKey(id)) {
			ParamStore ps = new ParamStore(space, designSpace, mappingStream);
			stores.put(id, ps);
		}
	}

	public static boolean exists(String id) {
		return stores.containsKey(id);
	}

	public Document getDesignSpaceTree() {
		return spaceTree;
	}

	public IDesignSpace getDesignSpace() {
		return space;
	}

	public Random getRNG() {
		return rng;
	}
}