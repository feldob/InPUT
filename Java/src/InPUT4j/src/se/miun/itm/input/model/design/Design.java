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
 */package se.miun.itm.input.model.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Comment;
import org.jdom2.Element;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.ExportHelper;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.element.ValueFactory;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.util.EnvironmentInfo;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * 
 * {@inheritDoc}
 * 
 * @NotThreadSafe
 */
public class Design implements IDesign {

	private static final Map<String, IDesign> designs = new HashMap<String, IDesign>();

	private Document design;

	// for read access in other classes, even though probably not necessary
	private ElementCache elementCache = new ElementCache();

	private final ParamStore ps;

	protected Design(final String expId, final ParamStore ps) throws InPUTException {
		this.ps = ps;
		addOptionalParamsToCache();
		design = initEmptyDesign(expId);
		design.getRootElement().setAttribute(Q.REF_ATTR, ps.getId());
		if (InPUTConfig.cachesDesigns())
			designs.put(ps.getId() + "." + expId, this); // store globally
		InPUTConfig.extendToConfigScope(this);
	}

	protected Design(final ParamStore ps, Document design) throws InPUTException {
		this.ps = ps;
		addOptionalParamsToCache();
		this.design = design;
		initValues();
		InPUTConfig.extendToConfigScope(this);
	}

	public Design(String filePath) throws InPUTException {
		design = SAXUtil.build(filePath, InPUTConfig.isValidationActive());
		String ref = design.getRootElement().getAttributeValue(Q.REF_ATTR);

		DesignSpace space = initDesignSpace(filePath, ref);
		ps = space.getParamStore();
		addOptionalParamsToCache();
		initValues();
		InPUTConfig.extendToConfigScope(this);
	}

	public Design(Design design) throws InPUTException {
		this(design.getParamStore(), design.getXML());
	}

	private void addOptionalParamsToCache() throws InPUTException {
		for (String optionalParamId : ps.getOptionalParamIds()) {
			elementCache.put(optionalParamId, null);
		}
	}

	private DesignSpace initDesignSpace(String filePath, String ref) throws InPUTException {
		if (ref == null) {
			throw new InPUTException("The 'ref' argument of the design by id '" + getId() + "' from file '" + filePath
					+ "' has to be set to the id of the respective design space.");
		}
		DesignSpace designSpace = DesignSpace.lookup(ref);
		if (designSpace != null)
			return designSpace;

		if (!ref.contains(Q.XML))
			ref += Q.XML;

		Document space = SAXUtil.build(ref, InPUTConfig.isValidationActive());
		return new DesignSpace(space, ref);
	}

	private void initValues() throws InPUTException {
		Element root = design.getRootElement();
		List<Element> obsoletes = root.getChildren();
		Element[] obsoletesA = obsoletes.toArray(new Element[] {});
		List<String> paramIds = new ArrayList<String>();
		for (int i = 0; i < obsoletesA.length; i++) {
			if (isValueE(obsoletesA[i])) {
				Value<?> newE = createElement(obsoletesA[i], root);
				paramIds.add(newE.getId());
				updateElementCache(newE);
			}
		}
		//
		// for (String paramId : paramIds) {
		// elementCache.get(paramId).getInputValue(null);
		// }
		validateInitialValues(paramIds);
	}

	private void validateInitialValues(List<String> paramIds) throws InPUTException {
		Param<?> param;
		Object value;
		Value<?> valueElement;
		for (String paramId : paramIds) {
			param = ps.getParam(paramId);
			if (!param.isIndependant()) {
				valueElement = elementCache.get(paramId);
				value = valueElement.getInputValue(null);
				if (valueElement.isArrayType())
					value = param.packArrayForExport(valueElement, value);
				param.validateInPUT(paramId, value, elementCache);
			}
		}
	}

	private Value<?> createElement(Element obsoleteE, Element root) throws InPUTException {
		Param<?> param;
		String id;
		// retrieve param id
		id = obsoleteE.getAttributeValue(Q.ID_ATTR);
		// retrieve meta information about the parameter
		param = ps.getParam(id);
		// create the new entry
		if (param == null) {
			throw new InPUTException("There is no parameter with id '" + id + "' in design space '" + ps.getId() + "'.");
		}
		Value<?> newE = ValueFactory.constructElementByElement(obsoleteE, param, param.getDimensions(), elementCache);
		// reset the obsolete entry
		{
			root.removeContent(obsoleteE);
			root.addContent(newE);
		}
		return newE;
	}

	protected Document initEmptyDesign(String expId) throws InPUTException {
		// hash = expId.hashCode();
		Document design = new Document(initEmptyRoot(expId));
		return design;
	}

	private Element initEmptyRoot(String expId) throws InPUTException {
		Element root = new Element(Q.DESIGN_ROOT, Q.DESIGN_NAMESPACE);
		root.addNamespaceDeclaration(Q.SCHEMA_INSTANCE_NAMESPACE);
		root.setAttribute(Q.SCHEMA_LOCATION_ATTR, Q.getSchemaLocation(), Q.SCHEMA_INSTANCE_NAMESPACE);
		root.setAttribute(Q.ID_ATTR, expId);

		if (ps.getDesignSpace().isFile()) {
			root.setAttribute(Q.REF_ATTR, ps.getDesignSpace().getFileName());
		}
		return root;
	}

	@Override
	public String getId() {
		return design.getRootElement().getAttributeValue(Q.ID_ATTR);
	}

	@Override
	public void resetId(String id) {
		design.getRootElement().setAttribute(Q.ID_ATTR, id);
	}

	@Override
	public <T> T getValue(final String paramId) throws InPUTException {
		return getValue(paramId, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(String paramId, Object[] actualParams) throws InPUTException {
		Value<?> element = elementCache.get(paramId);
		Object value = null;
		if (element != null) {
			value = element.getInputValue(actualParams);
			if (element.isArrayType())
				value = element.getParam().packArrayForExport(element, value);
		}
		return (T) value;
	}

	@Override
	public void setValue(final String paramId, Object value) throws InPUTException {
		if (isReadOnly())
			throw new InPUTException("The design is read only.");

		Param<?> param = ps.getParam(paramId);

		if (param != null)
			setValueForExplicitParam(paramId, value, param);
		else
			setValueForNoneExplicitParam(paramId, value);
	}

	private boolean isReadOnly() {
		return elementCache.isReadOnly();
	}

	private void setValueForExplicitParam(final String paramId, Object value, Param<?> param) throws InPUTException {
		param.validateInPUT(paramId, value, elementCache);
		if (param.isArrayType())
			value = ParamUtil.repackArrayForImport(value);

		Value<?> valueE = null;
		if (!(param.isOptional() && value == null)) {
			// create new element
			valueE = ValueFactory.constructElementByValue(value, param, param.getDimensions(), elementCache);
		}
		setElement(paramId, valueE);
	}

	private void setValueForNoneExplicitParam(final String paramId, Object value) throws InPUTException {
		Value<?> valueE;
		valueE = elementCache.get(paramId);
		if (valueE != null) {
			if (valueE.isParentInitialized()) // for configuration, the params
												// should be settable beforehand
				valueE.getParam().validateInPUT(paramId, value, elementCache);
			if (valueE.isArrayType()) {
				value = ParamUtil.repackArrayForImport(value);
			}
			valueE.setInputValue(value);
			// remove those parents that are effected from the cache.
			updateCacheForIndexedValue(valueE);
		} else
			throw new InPUTException("A parameter by name \"" + paramId + "\" does not exist.");
	}

	private void emptyCacheForChildren(Value<?> valueE) throws InPUTException {
		for (Object childValueE : valueE.getChildren()) {
			Value<?> childValue = (Value<?>) childValueE;
			emptyCache(childValue.getId(), childValue);
		}

	}

	private void updateCacheForIndexedValue(Value<?> parentValue) throws InPUTException {
		Element parent = parentValue.getParentElement();
		if (parent instanceof Value<?>) {
			updateElementCache(parentValue);
			parentValue = (Value<?>) parent;
			parentValue.getParam().init(parentValue, null, elementCache);
		}
	}

	void setElement(String paramId, Value<?> newValueE) throws InPUTException {
		// check validity
		Value<?> oldValueE = elementCache.get(paramId);

		if (!newValueE.getParam().isOptional() && isValid() && oldValueE == null)
			throw new InPUTException("The parameter \"" + paramId + "\" which you try to set is not part of design \"" + getId()
					+ "\". Is it a sub-parameter of an unset parameter choice or does it contain a spelling error?");

		addElement(paramId, newValueE);
	}

	private boolean isValid() {
		List<Element> highLevelParams = ps.getDesignSpaceTree().getRootElement().getChildren();
		for (Element param : highLevelParams) {
			if (param instanceof Param && !elementCache.containsKey(param.getAttributeValue(Q.ID_ATTR)))
				return false;
		}

		return true;
	}

	void addElement(String paramId, Value<?> valueE) throws InPUTException {
		Element parent = retrieveParent(paramId);
		// remove old element
		parent.removeContent(elementCache.get(paramId));

		if (valueE != null) {
			valueE.getParam().checkIfParameterSettable(paramId);
			// add new element
			parent.addContent(valueE);
			valueE.renewId();
		}
		// update index
		updateElementCache(paramId, valueE);
	}

	@Override
	public Void impOrt(InPUTImporter<Document> importer) throws InPUTException {
		design = importer.impOrt();
		initValues();
		return null;
	}

	protected boolean isValueE(Element obsoleteE) {
		String eName = obsoleteE.getName();
		return eName.equals(Q.SVALUE) || eName.equals(Q.NVALUE);
	}

	private void updateElementCache(Value<?> value) throws InPUTException {
		updateElementCache(value.getId(), value);
	}

	// thread safety has to be wrapped when calling
	private void updateElementCache(String paramId, final Value<?> valueE) throws InPUTException {
		// remove old entries
		emptyCache(paramId, valueE);

		// add the new entries.
		elementCache.put(paramId, valueE);

		if (valueE != null)
			for (Object childValueE : valueE.getChildren()) {
				Value<?> childValue = (Value<?>) childValueE;
				updateElementCache(childValue.getId(), childValue);
			}
	}

	private void emptyCache(String paramId, final Value<?> valueE) throws InPUTException {
		Value<?> obsoleteValue = elementCache.get(paramId);
		if (obsoleteValue != null && valueE != null) {
			emptyCacheForChildren(valueE);

			elementCache.remove(obsoleteValue.getId());
		}
	}

	private Element retrieveParent(String paramId) throws InPUTException {

		if (!ps.containsParam(paramId))
			throw new InPUTException("A parameter with id '" + paramId + "' is not specified in the InPUT file of this document type.");

		Value<?> valueE = elementCache.get(paramId);

		if (valueE == null)
			return design.getRootElement();
		else
			return valueE.getParentElement();
	}

	@Override
	public String getValueToString(String paramId) throws InPUTException {
		Object value = getValue(paramId);
		if (value != null)
			return value.toString();
		return null;
	}

	@Override
	public <O> O export(InPUTExporter<O> exporter) throws InPUTException {
		return exporter.export(design);
	}

	@Override
	public String toString() {
		return ExportHelper.exportableToString(this);
	}

	@Override
	public void setReadOnly() {
		elementCache.setReadOnly();
	}

	@Override
	public Set<String> getSupportedParamIds() {
		return elementCache.getSupportedParamIds();
	}

	@Override
	public IDesignSpace getSpace() {
		return ps.getDesignSpace();
	}

	@Override
	public void attachEnvironmentInfo() {
		String info = EnvironmentInfo.getInfo();
		design.addContent(new Comment(info));
	}

	@Override
	public void extendScope(IDesign neighbor) {
		if (neighbor != null && neighbor instanceof Design)
			elementCache.extendScope(((Design) neighbor).getElementCache());
	}

	private ElementCache getElementCache() {
		return elementCache;
	}

	/**
	 * lookup a registered design. the id is "designSpaceId.designId".
	 * 
	 * @param designId
	 * @return
	 */
	public static IDesign lookup(String designId) {
		return designs.get(designId);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Design))
			return false;
		return design.equals(((Design) obj).design);
	}

	@Override
	public boolean same(Object obj) {
		if (!(obj instanceof Design))
			return false;
		return elementCache.same(((Design) obj).elementCache);
	}

	protected ParamStore getParamStore() {
		return ps;
	}

	protected Document getXML() {
		return design;
	}

	@Override
	public IDesign toClone() throws InPUTException {
		return new Design(this);
	}
}