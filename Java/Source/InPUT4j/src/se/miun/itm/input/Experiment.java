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
package se.miun.itm.input;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.miun.itm.input.export.ExportHelper;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.impOrt.DocumentImporter;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.InputStreamWrapper;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * The default implementation of the IExperiment interface for InPUT4j.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class Experiment implements IExperiment {

	private final XMLFileImporter fileImporter = new XMLFileImporter();

	private final DocumentImporter docImporter = new DocumentImporter();

	private final List<IDesign> outputs = new ArrayList<IDesign>();

	private final Map<String, ByteArrayOutputStream> content = new HashMap<String, ByteArrayOutputStream>();

	private IDesign preferences;

	private IDesign algorithmDesign;

	private IDesign features;

	private final String id;

	private final IInPUT input;

	public Experiment(String id, IInPUT input) {
		this.id = id;
		this.input = input;
	}

	/**
	 * TODO unchecked cast for Design has to be fixed at some stage.
	 * @param id
	 * @param experiment
	 * @throws InPUTException
	 */
	public Experiment(String id, IExperiment experiment) throws InPUTException {
		this.id = id;
		this.input = experiment.getInPUT();
		
		IDesign design = experiment.getProblemFeatures();
		if (design != null){
			setProblemFeatures(new Design((Design)design));
		}
		
		design = experiment.getPreferences();
		if (design != null) {
			setPreferences(new Design((Design)design));
		}
		
		design = experiment.getAlgorithmDesign();
		if (design != null) {
			setAlgorithmDesign(new Design((Design)design));
		}
		
		addAllContent(experiment);
	}

	private void addAllContent(IExperiment experiment) throws InPUTException {
		for (String contentName : experiment.getContentNames()) {
			addContent(contentName, experiment.getContentFor(contentName));
		}
	}

	@Override
	public <T> T getValue(String paramId) throws InPUTException {
		return getValue(paramId, null);
	}

	@Override
	public String getValueToString(String paramId) throws InPUTException {
		String result;
		if (input.getAlgorithmDesignSpace() != null && algorithmDesign != null) {
			result = algorithmDesign.getValueToString(paramId);
			if (result != null)
				return result;
		}

		if (input.getPropertySpace() != null && preferences != null) {
			result = preferences.getValueToString(paramId);
			if (result != null)
				return result;

		}

		if (input.getProblemFeatureSpace() != null && features != null) {
			result = features.getValueToString(paramId);
			if (result != null)
				return result;
		}
		return null;
	}

	@Override
	public void importProblemFeatures(String featuresPath) throws InPUTException {
		fileImporter.resetFileName(featuresPath);
		features = input.getProblemFeatureSpace().impOrt(fileImporter);
	}

	@Override
	public void importAlgorithmDesign(String algorithmPath) throws InPUTException {
		fileImporter.resetFileName(algorithmPath);
		algorithmDesign = input.getAlgorithmDesignSpace().impOrt(fileImporter);
	}

	@Override
	public void importProperties(String preferencesPath) throws InPUTException {
		fileImporter.resetFileName(preferencesPath);
		preferences = input.getPropertySpace().impOrt(fileImporter);
	}

	@Override
	public IDesign getPreferences() {
		return preferences;
	}

	@Override
	public void setPreferences(IDesign preferences) {
		if (preferences != null && input.getPropertySpace() == null)
			throw new IllegalArgumentException("No propertySpace is defined, you cannot set preferences.");
		this.preferences = preferences;
		initScopes();
	}

	@Override
	public void setAlgorithmDesign(IDesign algorithmDesign) {
		if (algorithmDesign != null && input.getAlgorithmDesignSpace() == null)
			throw new IllegalArgumentException("No algorithm design space is defined, you cannot set an algorithm design.");
		this.algorithmDesign = algorithmDesign;
		initScopes();
	}

	@Override
	public void setProblemFeatures(IDesign features) {
		if (features != null && input.getProblemFeatureSpace() == null)
			throw new IllegalArgumentException("No problem feature space is defined, you cannot set problem features.");
		this.features = features;
		initScopes();
	}

	@Override
	public IDesign getAlgorithmDesign() {
		return algorithmDesign;
	}

	@Override
	public IDesign getProblemFeatures() {
		return features;
	}

	@Override
	public <T> T getValue(String paramId, Object[] actualParams) throws InPUTException {
		T result;
		if (input.getAlgorithmDesignSpace() != null && algorithmDesign != null) {
			result = algorithmDesign.getValue(paramId, actualParams);
			if (result != null)
				return result;
		}

		if (input.getPropertySpace() != null && preferences != null) {
			result = preferences.getValue(paramId, actualParams);
			if (result != null)
				return result;

		}

		if (input.getProblemFeatureSpace() != null && features != null) {
			result = features.getValue(paramId, actualParams);
			if (result != null)
				return result;
		}

		return null;
	}

	@Override
	public void setValue(String paramId, Object obj) throws InPUTException {
		if (input.getAlgorithmDesignSpace() != null && algorithmDesign != null)
			if (algorithmDesign.getValue(paramId) != null) {
				algorithmDesign.setValue(paramId, obj);
				return;
			}

		if (input.getPropertySpace() != null && preferences != null)
			if (preferences.getValue(paramId) != null) {
				preferences.setValue(paramId, obj);
				return;
			}

		if (input.getProblemFeatureSpace() != null && features != null)
			if (features.getValue(paramId) != null) {
				features.setValue(paramId, obj);
				return;
			}

		throw new InPUTException("There is no set parameter or property by name '" + paramId + "' defined in InPUT '" + id + "'.");
	}

	private void initDesigns(Map<String, Document> designs) throws InPUTException {
		features = initDesign(input.getProblemFeatureSpace(), designs.get(Q.PROBLEM_FEATURES_XML));
		preferences = initDesign(input.getPropertySpace(), designs.get(Q.PREFERENCES_XML));
		algorithmDesign = initDesign(input.getAlgorithmDesignSpace(), designs.get(Q.ALGORITHM_DESIGN_XML));
		initScopes();
	}

	private void initScopes() {
		extendScopeBy(preferences, features);
		extendScopeBy(algorithmDesign, features);
		extendScopeBy(algorithmDesign, preferences);
	}

	private void extendScopeBy(IDesign design, IDesign neighbor) {
		if (design != null)
			if (neighbor != null)
				design.extendScope(neighbor);
	}

	private IDesign initDesign(IDesignSpace space, Document document) throws InPUTException {
		if (document != null) {
			docImporter.resetContext(document);
			return space.impOrt(docImporter);
		}
		return null;
	}

	@Override
	public Void impOrt(InPUTImporter<Map<String, InputStreamWrapper>> importer) throws InPUTException, IOException {
		Map<String, InputStreamWrapper> documents = importer.impOrt();

		Map<String, Document> designs = new HashMap<String, Document>();
		
		for (String id : documents.keySet())
			if (ExperimentArchiveImporter.isExperimentalFile(id))
				designs.put(id,SAXUtil.build(documents.get(id).next(), false));
			else{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				InputStreamWrapper.fromInputStreamToOutputStream(documents.get(id).next(), out);
				addContent(id, out);
			}
		
		initDesigns(designs);

		int i = 1;
		while (designs.containsKey(Q.OUTPUT + i + Q.XML)) {
			outputs.add(initDesign(input.getOutputSpace(), designs.get(Q.OUTPUT + i + Q.XML)));
			i++;
		}

		return null;
	}

	@Override
	public void addOutput(IDesign output) {
		outputs.add(output);
	}

	@Override
	public void addOutput(List<IDesign> outputs) {
		this.outputs.addAll(outputs);
	}

	@Override
	public void clearOutput() {
		outputs.clear();
	}

	@Override
	public List<IDesign> getOutput() {
		return Collections.unmodifiableList(outputs);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public <O> O export(InPUTExporter<O> exporter) throws InPUTException {
		return exporter.export(this);
	}

	@Override
	public IInPUT getInPUT() {
		return input;
	}

	@Override
	public boolean investigatesSameConfiguration(IExperiment experiment) {
		if (!experiment.getInPUT().equals(input))
			return false;

		if (!sameExists(experiment.getAlgorithmDesign(), algorithmDesign) || !sameExists(experiment.getPreferences(), preferences)
				|| !sameExists(experiment.getProblemFeatures(), features))
			return false;

		return true;
	}

	public boolean sameExists(IDesign first, IDesign second) {

		if (first == null || second == null)
			return (first == null && second == null); // True if both are null, false if any are null.

		// We know neither are null.
		return first.same(second);
	}

	@Override
	public boolean same(IExperiment foreigner) {
		if (foreigner == null)
			return false;
		
		if ((algorithmDesign != null && !algorithmDesign.same(foreigner.getAlgorithmDesign()))
				|| (preferences != null && !preferences.same(foreigner.getPreferences()))
				|| (features != null && !features.same(foreigner.getProblemFeatures())))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return ExportHelper.exportableToString(this);
	}

	@Override
	public void addContent(String name, ByteArrayOutputStream contentAsStream) throws InPUTException {
		content.put(name, contentAsStream);
	}

	@Override
	public Set<String> getContentNames() {
		return content.keySet();
	}

	@Override
	public ByteArrayOutputStream getContentFor(String identifier) {
		return content.get(identifier);
	}
}