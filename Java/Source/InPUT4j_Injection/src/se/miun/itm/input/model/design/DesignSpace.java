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

import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

import se.miun.itm.input.eval.ParamEvaluationOrderComparator;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.Dimensions;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.element.ValueFactory;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.TreeSorter;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * 
 * @author Felix Dobslaw
 */
public class DesignSpace implements IDesignSpace {

	private static final Map<String, DesignSpace> spaces = new HashMap<String, DesignSpace>();
	
	private final ParamEvaluationOrderComparator<Element> comparator = new ParamEvaluationOrderComparator<Element>();

	private final Document space;

	private final ParamStore ps;

	private final String id;
	
	private final int hash;

	private final boolean isFile;

	private String fileName;

	public DesignSpace(InputStream spaceStream, InputStream mappingStream)
			throws InPUTException {
		isFile = false;
		space = initSpace(spaceStream);
		id = space.getRootElement().getAttributeValue(Q.ID_ATTR);
		hash = id.hashCode();
		ps = initParamStore(mappingStream);
		DesignSpace.register(id,this);
	}
	
	public DesignSpace(String filePath) throws InPUTException{
		this(filePath, null);
	}

	public DesignSpace(String filePath, InputStream mappingStream)
			throws InPUTException {
		isFile = true;
		fileName = new File(filePath).getName();
		try {
			space = initSpace(new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			throw new InPUTException(e.getMessage(),e);
		}
		id = space.getRootElement().getAttributeValue(Q.ID_ATTR);
		hash = id.hashCode();
		ps = initParamStore(mappingStream);
		DesignSpace.register(id,this);
	}

	public DesignSpace(InputStream spaceStream) throws InPUTException {
		this(spaceStream, null);
	}

	public DesignSpace(Document space, String spaceFileName) throws InPUTException {
		this.isFile = true;
		this.fileName = spaceFileName;
		this.space = space;
		id = space.getRootElement().getAttributeValue(Q.ID_ATTR);
		hash = id.hashCode();
		String mappingPath = space.getRootElement().getAttributeValue(Q.MAPPING_ATTR);
		//TODO does not need mapping, then it should not be needed here!
		if (mappingPath != null) {
			try {
				ps = initParamStore(new FileInputStream(mappingPath));
			} catch (Exception e) {
				throw new InPUTException(e.getMessage(),e);
			}
		}else{
			ps = initParamStore(null);
		}
		DesignSpace.register(id,this);
	}

	private Document initSpace(InputStream spaceStream) throws InPUTException {
		Document space = SAXUtil.build(spaceStream, false);

		String id = space.getRootElement()
				.getAttributeValue(Q.ID_ATTR);
		if (ParamStore.exists(id)) {
			space = ParamStore.getInstance(id).getDesignSpaceTree();
		}
		return space;
	}

	private ParamStore initParamStore(InputStream mappingStream)
			throws InPUTException {
		ParamStore ps;
		if (!ParamStore.exists(id)) {
			ParamStore.register(this, mappingStream, space);
			// reorganize the tree so that parameters are initialized in the
			// right dependency order
			TreeSorter.reorganizeTree(space.getRootElement(), comparator);
			ps = ParamStore.getInstance(id);
		} else {
			ps = ParamStore.getInstance(id);
		}
		return ps;
	}

	@Override
	public String getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId) throws InPUTException {
		return (T) nextElement(paramId, ps.getParam(paramId).getDimensions(),
				new HashMap<String, Object>(), null).getInputValue(null);
	}

	@Override
	public IDesign impOrt(InPUTImporter<Document> importer)
			throws InPUTException {
		return new Design(ps, importer.impOrt());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId, Integer[] sizeArray)
			throws InPUTException {
		Object[] entries = new Object[sizeArray[0]];
		if (sizeArray[0] == 0)
			entries = new Object[1];
		if (sizeArray.length == 1)
			for (int i = 0; i < entries.length; i++)
				entries[i] = nextElement(paramId, Dimensions.DEFAULT_DIM,
						new HashMap<String, Object>(), null)
						.getInputValue(null);
		else
			for (int i = 0; i < entries.length; i++)
				entries[i] = next(paramId,
						Arrays.copyOfRange(sizeArray, 1, sizeArray.length));
		return (T) entries;
	}

	@Override
	public IDesign nextEmptyDesign(String designId) throws InPUTException {
		return new Design(designId, ps);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IDesign nextDesign(String expId) throws InPUTException {

		Design design = new Design(expId, ps);

		List<Param> children = (List<Param>)(List<?>)space.getRootElement().getChildren();

		Map<String, Object> vars = new HashMap<String, Object>();
		for (Param param : children)
			setRandom(design, vars, param);

		return design;
	}

	private void setRandom(Design design, Map<String, Object> vars,
			Param param) throws InPUTException {
		Value<? extends Param> random;
		random = nextElement(param.getId(), param.getDimensions(), vars,
				null);
		design.setElement(param.getId(), random);
	}

	@Override
	public IDesign nextDesign(String expId, boolean readOnly)
			throws InPUTException {
		IDesign nextDesign = nextDesign(expId);
		if (readOnly)
			nextDesign.setReadOnly();
		return nextDesign;
	}

	private Value<?> nextElement(String paramId, Object[] actualParents)
			throws InPUTException {
		return nextElement(paramId, ps.getParam(paramId).getDimensions(),
				new HashMap<String, Object>(), actualParents);
	}

	private Value<? extends Param> nextElement(String paramId, Integer[] sizeArray,
			Map<String, Object> vars, Object[] actualParents)
			throws InPUTException {
		return ValueFactory.constructRandomElement(ps.getParam(paramId),
				sizeArray, vars, actualParents, null);
	}

	private Value<?> nextElement(String paramId, Map<String, Object> vars)
			throws InPUTException {
		return nextElement(paramId, ps.getParam(paramId).getDimensions(), vars,
				null);
	}

	private Value<?> nextElement(String paramId, Integer[] sizeArray)
			throws InPUTException {
		return nextElement(paramId, sizeArray, new HashMap<String, Object>(),
				null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId, Object[] actualParams)
			throws InPUTException {
		return (T) nextElement(paramId, actualParams).getInputValue(
				actualParams);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId, Map<String, Object> vars)
			throws InPUTException {
		if (vars == null || vars.isEmpty())
			return (T) next(paramId);
		return (T) nextElement(paramId, vars).getInputValue(null);
	}

	@SuppressWarnings("unchecked")
	public <T> T next(String paramId, Integer[] sizeArray,
			Map<String, Object> vars) throws InPUTException {
		Object value;
		if (vars == null || vars.isEmpty())
			value = next(paramId, sizeArray);
		else
			value = nextElement(paramId, sizeArray, vars, null).getInputValue(
					null);
		return (T) value;
	}

	@Override
	public <O> O export(InPUTExporter<O> exporter) throws InPUTException {
		return exporter.export(space);
	}

	@Override
	public String toString() {
		return space.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId, Integer[] sizeArray, Object[] actualParams)
			throws InPUTException {
		return (T) nextElement(paramId, sizeArray).getInputValue(actualParams);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId, Map<String, Object> vars,
			Object[] actualParams) throws InPUTException {
		return (T) nextElement(paramId, ps.getParam(paramId).getDimensions(),
				vars, actualParams).getInputValue(actualParams);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T next(String paramId, Integer[] sizeArray,
			Map<String, Object> vars, Object[] actualParams)
			throws InPUTException {
		return (T) nextElement(paramId, sizeArray, vars, actualParams)
				.getInputValue(actualParams);
	}

	@Override
	public Set<String> getSupportedParamIds() {
		return ps.getParamIds();
	}
	

	static void register(String id, DesignSpace designSpace) {
		spaces.put(id, designSpace);
	}
	
	static void unRegister(String id) {
		spaces.remove(id);
	}

	public static DesignSpace lookup(String id) {
		return spaces.get(id);
	}

	ParamStore getParamStore() {
		return ps;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean isFile() {
		return isFile;
	}

	@Override
	public String getFileName() {
		return fileName;
	}
}