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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * The default implementation of the IInPUT interface for InPUT4j.
 * @author Felix Dobslaw
 */
public class InPUT implements IInPUT {

	private final IDesignSpace propertySpace;

	private final IDesignSpace algorithmSpace;

	private final IDesignSpace problemFeatureSpace;

	private final IDesignSpace outputSpace;

	private final String id;

	private final int hash;

	public InPUT(String inputId, String algorithmSpaceFilePath,
			String propertySpaceFilePath, String problemFeatureSpaceFilePath,
			String outputSpaceFilePath) throws InPUTException,
			FileNotFoundException {
		this(inputId, algorithmSpaceFilePath != null ? new FileInputStream(
				algorithmSpaceFilePath) : null,
				propertySpaceFilePath != null ? new FileInputStream(
						propertySpaceFilePath) : null,
				problemFeatureSpaceFilePath != null ? new FileInputStream(
						problemFeatureSpaceFilePath) : null,
				outputSpaceFilePath != null ? new FileInputStream(
						outputSpaceFilePath) : null);
	}

	public InPUT(String inputId, InputStream algorithmDesignSpaceStream,
			InputStream propertySpaceStream, InputStream problemSpaceStream,
			InputStream outputSpaceStream) throws InPUTException {
		id = inputId;
		hash = id.hashCode();
		problemFeatureSpace = initDesignSpace(problemSpaceStream);
		propertySpace = initDesignSpace(propertySpaceStream);
		algorithmSpace = initDesignSpace(algorithmDesignSpaceStream);
		outputSpace = initDesignSpace(outputSpaceStream);
	}

	public InPUT(String id, IDesignSpace algorithmSpace,
			IDesignSpace propertySpace, IDesignSpace problemSpace,
			IDesignSpace outputSpace) {
		this.id = id;
		hash = id.hashCode();
		this.algorithmSpace = algorithmSpace;
		this.propertySpace = propertySpace;
		this.problemFeatureSpace = problemSpace;
		this.outputSpace = outputSpace;
	}

	private IDesignSpace initDesignSpace(InputStream spaceStream)
			throws InPUTException {
		if (spaceStream != null)
			return new DesignSpace(spaceStream);
		return null;
	}

	@Override
	public IDesignSpace getProblemFeatureSpace() {
		return problemFeatureSpace;
	}

	@Override
	public IDesignSpace getAlgorithmDesignSpace() {
		return algorithmSpace;
	}

	@Override
	public IDesignSpace getPropertySpace() {
		return propertySpace;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public <T> T export(InPUTExporter<T> exporter) throws InPUTException {
		return exporter.export(this);
	}

	@Override
	public IDesignSpace getOutputSpace() {
		return outputSpace;
	}

	public static IInPUT getInPUT(InPUTImporter<IInPUT> importer)
			throws InPUTException {
		return importer.impOrt();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public IExperiment impOrt(String id,
			InPUTImporter<Map<String, Document>> importer)
			throws InPUTException {
		Experiment experiment = new Experiment(id, this);
		experiment.impOrt(importer);
		return experiment;
	}
}