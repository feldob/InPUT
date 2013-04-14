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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.InputStreamWrapper;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * The standard implementation for InPUT4j of code mappings via IMappings.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class Mappings implements IMappings {

	private static Map<String, IMappings> inputs = new HashMap<String, IMappings>();

	private static Map<String, Set<String>> references = new HashMap<String, Set<String>>();

	private final Map<String, IMapping> mappings = new HashMap<String, IMapping>();

	private final Map<String, IMapping> types = new HashMap<String, IMapping>();

	private final Map<String, String> alias = new HashMap<String, String>();

	private final String id;

	private final int hash;

	private final Document document;

	private Mappings(String id, String codeMappingFilePath) throws InPUTException {
		this.id = id;
		hash = id.hashCode();
		document = initTypes(codeMappingFilePath);
		initDependencies();
	}

	private Mappings(String inputId, InputStream codeMappingStream) throws InPUTException {
		this.id = inputId;
		hash = id.hashCode();
		Document xml = SAXUtil.build(codeMappingStream, false);
		document = initMappings(xml);
		initDependencies();
	}

	private Mappings(String mappingId, Document mapping) throws InPUTException {
		this.id = mappingId;
		hash = id.hashCode();
		document = initMappings(mapping);
		initDependencies();
	}

	private void initDependencies() {
		List<IMapping> entries = new ArrayList<IMapping>(mappings.values());
		for (int i = 0; i < entries.size(); i++)
			for (int j = i + 1; j < entries.size(); j++)
				initDependencies(entries.get(i), entries.get(j));
	}

	private void initDependencies(IMapping mapping, IMapping mapping2) {
		if (mapping instanceof StructuralMapping){
			if (((StructuralMapping) mapping).containsInConstructorSignature(mapping2.getId()))
				mapping.addDependee(mapping2);
		} else if (mapping2 instanceof StructuralMapping)
				if (((StructuralMapping) mapping2).containsInConstructorSignature(mapping.getId()))
					mapping2.addDependee(mapping);
	}

	private Document initTypes(String codeMappingFilePath) throws InPUTException {

		Document document = SAXUtil.build(codeMappingFilePath, InPUTConfig.isValidationActive());

		return initMappings(document);
	}

	private Document initMappings(Document xml) throws InPUTException {
		// log.info("Setting up the implementation specific mappings...");
		List<Element> mappingElements = xml.getRootElement().getChildren();
		for (Element mappingElement : mappingElements) {
			initMapping(mappingElement);
		}
		// log.info("... Mappings successfully imported.");
		return xml;
	}

	private void initMapping(Element mappingElement) throws InPUTException {
		if (mappingElement.getName().equals(Q.MAPPING))
			initRealMapping(mappingElement);
		else if (mappingElement.getName().equals(Q.MAPPING_TYPE))
			initTypeMapping(mappingElement);
	}

	private void initRealMapping(Element mappingElement) throws InPUTException {
		IMapping mapping;
		String type = mappingElement.getAttributeValue(Q.TYPE_ATTR);
		String id = mappingElement.getAttributeValue(Q.ID_ATTR);
		mapping = createMapping(mappingElement, type, id);
		mappings.put(mapping.getId(), mapping);
	}

	private IMapping createMapping(Element mappingElement, String type, String id) throws InPUTException {
		IMapping mapping;
		if (type != null && types.containsKey(type)) {
			mapping = types.get(type).clone(id, mappingElement);
		} else
			mapping = getInstance(mappingElement);
		return mapping;
	}

	private IMapping getInstance(Element mappingElement) throws InPUTException {
		String mappingType = mappingElement.getAttributeValue(Q.TYPE_ATTR);

		IMapping mapping;
		if (mappingType == null)
			mapping = new NumericMapping(mappingElement);
		else
			mapping = new StructuralMapping(mappingElement);
		return mapping;
	}

	private void initTypeMapping(Element mappingElement) throws InPUTException {
		IMapping mapping;
		mapping = getInstance(mappingElement);
		types.put(mapping.getId(), mapping);
	}

	@Override
	public String getComponentId(String paramId) {
		return ((StructuralMapping) mappings.get(alias(paramId))).getComponentType();
	}

	private String alias(String paramId) {
		if (alias.containsKey(paramId))
			return alias.get(paramId);
		else
			return paramId;
	}

	@Override
	public void addAlias(String alias, String paramId) {
		this.alias.put(alias, paramId);
	}

	public static String initMapping(String codeMappingFilePath) throws InPUTException {
		String inputId = ParamUtil.extractDescriptorId(codeMappingFilePath);
		IMappings mappings = new Mappings(inputId, codeMappingFilePath);
		initMapping(inputId, mappings);
		return inputId;
	}

	public static String initMapping(InputStream codeMappingStream) throws InPUTException, IOException {
		InputStreamWrapper inputStream = new InputStreamWrapper(codeMappingStream);
		String inputId = ParamUtil.extractDescriptorId(inputStream.next());
		IMappings mappings = new Mappings(inputId, inputStream.next());
		initMapping(inputId, mappings);
		return inputId;
	}

	public static void initMapping(Document mapping) throws InPUTException {
		String mappingId = mapping.getRootElement().getAttributeValue(Q.ID_ATTR);
		IMappings mappings = new Mappings(mappingId, mapping);
		initMapping(mappingId, mappings);
	}

	private static void initMapping(String inputId, IMappings mappings) throws InPUTException {
		if (!inputs.containsKey(inputId)) {
			inputs.put(inputId, mappings);
			references.put(inputId, new HashSet<String>());
		}
	}

	public static void releaseMapping(String inputId) {
		if (inputs.containsKey(inputId)) {
			for (String referingId : references.get(inputId)) {
				inputs.remove(referingId);
				references.remove(referingId);
			}
			inputs.remove(inputId);
			references.remove(inputId);
		}
	}

	public static void register(String designId, String inputId) {
		if (inputs.containsKey(inputId)) {
			inputs.put(designId, inputs.get(inputId));
			references.get(inputId).add(designId);
		}
	}

	public static IMappings getInstance(String inputId) {
		return inputs.get(inputId);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public IMapping getMapping(String paramId) {
		// if a parameter is both, alias and type, merge them!
		String alias = alias(paramId);
		if (alias.equals(paramId) || !mappings.containsKey(paramId)) {
			return mappings.get(alias);
		} else {
			return merge(mappings.get(alias), mappings.get(paramId));
		}
	}

	private IMapping merge(IMapping abstractMapping, IMapping concreteMapping) {
		if (abstractMapping == null)
			return concreteMapping;

		IMapping mapping = abstractMapping.clone(id, concreteMapping);

		// now, substitute the mapping; the alias is not needed anymore!
		mappings.put(mapping.getId(), mapping);
		alias.remove(mapping.getId());

		return mapping;
	}

	@Override
	public Collection<String> getMappingParameterIds() {
		return mappings.keySet();
	}

	@Override
	public <O> O export(InPUTExporter<O> exporter) throws InPUTException {
		return exporter.export(document);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	public static void releaseAllMappings() {
		inputs.clear();
	}
}