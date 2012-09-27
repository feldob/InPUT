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

package se.miun.itm.input.model.design;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom2.Comment;
import org.jdom2.Element;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.element.ValueFactory;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.util.EnvironmentInfo;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * 
 * @author Felix Dobslaw
 */
public class Design implements IDesign {

	private Document design;

	// for read access in other classes, even though probably not necessary
	private ElementCache elementCache = new ElementCache();

	private final ParamStore ps;

	private int hash;

	protected Design(final String expId, final ParamStore ps) throws InPUTException {
		hash = expId.hashCode();
		this.ps = ps;
		this.design = initEmptyDesign(expId);
		InPUTConfig.extendToConfigScope(this);
	}

	protected Design(final ParamStore ps, Document design)
			throws InPUTException {
		hash = design.getRootElement().getAttributeValue(Q.ID_ATTR)
				.hashCode();
		this.design = design;
		this.ps = ps;
		initValues();
		InPUTConfig.extendToConfigScope(this);
	}

	public Design(String filePath) throws InPUTException {
		// parse the design
		design = SAXUtil.build(filePath, true);
		String spaceFile = design.getRootElement().getAttributeValue(
				Q.REF_ATTR);
		Document space = SAXUtil.build(spaceFile, true);
		String spaceId = space.getRootElement().getAttributeValue(
				Q.ID_ATTR);
		DesignSpace designSpace = DesignSpace.lookup(spaceId);
		if (designSpace == null) {
			designSpace = new DesignSpace(space, spaceFile);
		}
		hash = design.getRootElement().getAttributeValue(Q.ID_ATTR)
				.hashCode();
		ps = designSpace.getParamStore();
		initValues();
		InPUTConfig.extendToConfigScope(this);
	}

	/**
	 * contract: only promisses to work if one of two true: the design file in
	 * filePath contains a ref="spaceFile.xml" that is valid or the param
	 * spaceFile points to a valid space xml file.
	 * 
	 * @param filePath
	 * @param spaceFile
	 * @throws InPUTException
	 */
	public Design(String filePath, String spaceFile) throws InPUTException {
		design = SAXUtil.build(filePath, true);
		String fileSpaceFile = design.getRootElement().getAttributeValue(
				Q.REF_ATTR);

		Document space = lookupSpace(spaceFile, fileSpaceFile);

		String spaceId = space.getRootElement().getAttributeValue(
				Q.ID_ATTR);
		DesignSpace designSpace = DesignSpace.lookup(spaceId);
		if (designSpace == null) {
			designSpace = new DesignSpace(space, spaceFile);
		}
		hash = design.getRootElement().getAttributeValue(Q.ID_ATTR)
				.hashCode();
		ps = designSpace.getParamStore();
		initValues();
		InPUTConfig.extendToConfigScope(this);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	private Document lookupSpace(String spaceFile, String fileSpaceFile)
			throws InPUTException {
		Document space = null;
		boolean flag = false;
		if (fileSpaceFile != null) {
			try {
				space = SAXUtil.build(fileSpaceFile, true);
			} catch (Exception e) {
				// could not find file, choose the other one
				flag = true;
			}
		} else {
			flag = true;
		}
		if (flag) {
			space = SAXUtil.build(spaceFile, true);
		}
		return space;
	}

	@SuppressWarnings("unchecked")
	private void initValues() throws InPUTException {
		Element root = design.getRootElement();
		List<Element> obsoletes = root.getChildren();
		Element[] obsoletesA = obsoletes.toArray(new Element[] {});
		for (int i = 0; i < obsoletesA.length; i++) {
			if (isValueE(obsoletesA[i])) {
				Value<?> newE = createElement(obsoletesA[i], root);
				updateElementCache((Value<Param>) newE);
			}
		}
	}

	private Value<?> createElement(Element obsoleteE, Element root)
			throws InPUTException {
		Param param;
		String id;
		// retrieve param id
		id = obsoleteE.getAttributeValue(Q.ID_ATTR);
		// retrieve meta information about the parameter
		param = ps.getParam(id);
		// create the new entry
		if (param == null) {
			throw new InPUTException("There is no parameter with id '" + id
					+ "' in design space '" + ps.getId() + "'.");
		}
		Value<?> newE = ValueFactory.constructElementByElement(
				obsoleteE, param, param.getDimensions(), elementCache);
		// reset the obsolete entry
		{
			root.removeContent(obsoleteE);
			root.addContent(newE);
		}
		return newE;
	}

	protected Document initEmptyDesign(String expId) throws InPUTException {
		hash = expId.hashCode();
		Document design = new Document(initEmptyRoot(expId));
		return design;
	}

	private Element initEmptyRoot(String expId) throws InPUTException {
		Element root = new Element(Q.DESIGN_ROOT,
				Q.DESIGN_NAMESPACE);
		root.addNamespaceDeclaration(Q.SCHEMA_INSTANCE_NAMESPACE);
		root.setAttribute(Q.SCHEMA_LOCATION_ATTR, Q.getSchemaLocation(),
				Q.SCHEMA_INSTANCE_NAMESPACE);
		root.setAttribute(Q.ID_ATTR, expId);
		
		if (ps.getDesignSpace().isFile()) {
			root.setAttribute(Q.REF_ATTR, ps.getDesignSpace().getFileName());
		}
		return root;
	}

	public String getId() {
		return design.getRootElement().getAttributeValue(Q.ID_ATTR);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(final String paramId) throws InPUTException {
		return (T) getValue(paramId, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(String paramId, Object[] actualParams)
			throws InPUTException {
		Value<?> element = elementCache.get(paramId);
		Object value = null;
		if (element != null)
		{
			value = element.getInputValue(actualParams);
			if (element.isArrayType())
				value = ParamUtil.packArrayForExport(element.getParam().getInPUTClass(), value, element.getDimensions());
		}
		return (T) value;
	}

	@Override
	public void setValue(final String paramId, Object value)
			throws InPUTException {
		Value<? extends Param> valueE;
		Param param = ps.getParam(paramId);
		if (param != null) {
			if (param.isArrayType())
				value = ParamUtil.repackArrayForImport(value);
			// create new element
			valueE = ValueFactory.constructElementByValue(value, param,
					param.getDimensions(), elementCache);
			setElement(paramId, valueE);
		} else {
			valueE = elementCache.get(paramId);
			if (valueE != null){
				if (valueE.isArrayType())
					value = ParamUtil.repackArrayForImport(value);
				valueE.setInputValue(value);
				//remove those parents that are effected from the cache.
				elementCache.updateCache(valueE);
			}
		}
	}

	void setElement(String paramId, Value<? extends Param> valueE)
			throws InPUTException {
		// check validity and retrieve parent
		Element parent = checkInputValidity(paramId);
		// remove old element
		parent.removeContent(elementCache.get(paramId));
		// add new element
		parent.addContent(valueE);
		// update index
		updateElementCache(valueE);
	}

	@Override
	public Void impOrt(InPUTImporter<Document> importer) throws InPUTException {
		design = importer.impOrt();
		initValues();
		return null;
	}

	protected boolean isValueE(Element obsoleteE) {
		String eName = obsoleteE.getName();
		return eName.equals(Q.SVALUE)
				|| eName.equals(Q.NVALUE);
	}

	// thread safety has to be wrapped when calling
	@SuppressWarnings("unchecked")
	private void updateElementCache(final Value<? extends Param> valueE) {
		// remove old entries
		emptyCache(valueE);

		// add the new entries.
		elementCache.put(valueE.getId(), valueE);

		for (Object childValueE : valueE.getChildren())
			updateElementCache((Value<? extends Param>) childValueE);
	}

	private void emptyCache(final Value<? extends Param> valueE) {
		Value<?> obsoleteValue = elementCache.get(valueE.getId());
		if (obsoleteValue != null) {
			for (Object childValueE : valueE.getChildren()) {
				emptyCache((Value<?>) childValueE);
			}

			elementCache.remove(obsoleteValue.getId());
		}
	}

	private Element checkInputValidity(String paramId) throws InPUTException {
		String[] idPath = paramId.split(Pattern.quote("."));
		Element parent;

		if (!ps.containsParam(paramId)) {
			throw new InPUTException(
					"A parameter with id '"
							+ paramId
							+ "' is not specified in the InPUT file of this document type.");
		}

		if (idPath.length > 2) {
			String choiceId = idPath[idPath.length - 2];
			String parentParamId = idPath[idPath.length - 3];
			// a guess is that the currently set parent parameter is not set
			// to
			// the requested value
			if (!elementCache.containsKey(paramId)) {
				throw new InPUTException("The parameter '" + paramId
						+ "' you try to add requires it's parent '"
						+ parentParamId + "' to be set to '" + choiceId + "'.");
			} else
				parent = elementCache.get(parentParamId);
		} else 
			parent = design.getRootElement();

		// check if the value is set via constructor: then this action
		// is not allowed!
		Element param = ps.getParam(paramId).getParentElement();
		if (param instanceof AStruct) {
			if (((AStruct) param)
					.isInitByConstructor(idPath[idPath.length - 1])) {
				throw new InPUTException(
						"The parameter you try to set cannot be set that way, as it is instantiated via constructor of its parent parameter. You will have to reset the parent parameter in order to change it.");
			}
		}
		return parent;
	}

	public String getValueToString(String paramId) throws InPUTException {
		Value<?> valueElement = getValue(paramId);
		if (valueElement != null)
			return valueElement.valueToString();
		return null;
	}

	@Override
	public <O> O export(InPUTExporter<O> exporter) throws InPUTException {
		return exporter.export(design);
	}

	@Override
	public String toString() {
		return design.toString();
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
		if (neighbor instanceof Design)
			elementCache.extendScope(((Design)neighbor).getElementCache());
	}

	private ElementCache getElementCache() {
		return elementCache;
	}
}