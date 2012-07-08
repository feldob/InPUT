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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.impOrt.DocumentImporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.Q;
/**
 * The default implementation of the IExperiment interface for InPUT4j.
 * @author Felix Dobslaw
 */
public class Experiment implements IExperiment{

	private final XMLFileImporter fileImporter = new XMLFileImporter();

	private final DocumentImporter docImporter = new DocumentImporter();
	
	private final List<IDesign> outputs = new ArrayList<IDesign>();

	private IDesign preferences;

	private IDesign algorithmDesign;

	private IDesign features;

	private final String id;
	
	private final IInPUT input;

	private final int hash;

	public Experiment(String id, IInPUT input) {
		this.id = id;
		this.hash = id.hashCode();
		this.input = input;
	}
	
	@Override
	public <T> T getValue(String paramId) throws InPUTException {
		return getValue(paramId, null);
	}

	@Override
	public String getValueToString(String paramId) {
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
	public void importProblemFeatures(String featuresPath)
			throws InPUTException {
			fileImporter.resetFileName(featuresPath);
			features = input.getProblemFeatureSpace().impOrt(fileImporter);
	}
	
	@Override
	public void importAlgorithmDesign(String algorithmPath)
			throws InPUTException {
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
		if (input.getPropertySpace() == null)
			throw new IllegalArgumentException(
					"No propertySpace is defined, you cannot set preferences.");
		this.preferences = preferences;
	}

	@Override
	public void setAlgorithmDesign(IDesign algorithmDesign) {
		if (input.getAlgorithmDesignSpace() == null)
			throw new IllegalArgumentException(
					"No algorithm design space is defined, you cannot set an algorithm design.");
		this.algorithmDesign = algorithmDesign;

	}

	@Override
	public IDesign getAlgorithmDesign() {
		return algorithmDesign;
	}

	@Override
	public void setProblemFeatures(IDesign features) {
		if (input.getProblemFeatureSpace() == null)
			throw new IllegalArgumentException(
					"No problem feature space is defined, you cannot set problem features.");
		this.features = features;
	}

	@Override
	public IDesign getProblemFeatures() {
		return features;
	}

	@Override
	public <T> T getValue(String paramId, Object[] actualParams)
			throws InPUTException {
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

		throw new InPUTException(
				"There is no set parameter or property by name '" + paramId
						+ "' defined in InPUT '" + id + "'.");
	}
	

	private void initDesigns(Map<String, Document> designs) throws InPUTException {
		features = initDesign(input.getProblemFeatureSpace(),
				designs.get(Q.PROBLEM_FEATURES_XML));
		preferences = initDesign(input.getPropertySpace(),
				designs.get(Q.PREFERENCES_XML));
		algorithmDesign = initDesign(input.getAlgorithmDesignSpace(),
				designs.get(Q.ALGORITHM_DESIGN_XML));
		initScopes();
	}

	private void initScopes() {
		extendScopeBy(preferences, features);
		extendScopeBy(algorithmDesign, features);
		extendScopeBy(algorithmDesign, preferences);
	}

	private void extendScopeBy(IDesign design, IDesign neighbor) {
		if (design!= null)
			if (neighbor != null)
				design.extendScope(neighbor);
	}

	private IDesign initDesign(IDesignSpace space, Document document)
			throws InPUTException {
		if (document != null) {
				docImporter.resetContext(document);
				return space.impOrt(docImporter);
		}
		return null;
	}
	
	@Override
	public Void impOrt(InPUTImporter<Map<String, Document>> importer)
			throws InPUTException {
		Map<String, Document> designs = importer.impOrt();
		
		initDesigns(designs);
		
		int i = 1;
		while (designs.containsKey(Q.OUTPUT_XML + i)) {
			outputs.add(initDesign(input.getOutputSpace(),
					designs.get(Q.OUTPUT_XML + i)));
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
	public int hashCode() {
		return hash;
	}
}