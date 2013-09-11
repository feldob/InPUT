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
 */package se.miun.itm.input;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.miun.itm.input.export.ZipFileExporter;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.impOrt.InPUTArchiveImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

/**
 * An <code>ExperimentConductor</code> is an abstract template for the conduct of experiments, which is supposed to be 
 * inherited for the specific needs of the specific experiment. Here, the generic T defines the type of the expected result of the
 * execution of the conductor. This could, for instance, be an output of type IDesign.
 *  
 * @author Felix Dobslaw
 *
 * @param <T>
 */
public abstract class ExperimentConductor<T> {

	private static final InPUTArchiveImporter importer = new InPUTArchiveImporter();

	private static final ExperimentArchiveImporter experimentImporter = new ExperimentArchiveImporter();

	private static final ZipFileExporter zipExporter = new ZipFileExporter();

	private final Map<String, String> experimentIds = new HashMap<String, String>();

	private IInPUT input;

	/**
	 * A conductor for an investigation index of the given <code>IInPUT</code> type.
	 * @param input
	 */
	public ExperimentConductor(IInPUT input) {
		this.input = input;
	}

	/**
	 * A conductor that imports the <code>IInPUT</code> in archive form from the given <code>filePath</code> with id <code>id</code>. 
	 * @param id
	 * @param filePath
	 * @throws InPUTException
	 */
	public ExperimentConductor(String id, String filePath) throws InPUTException {
		this.input = getInPUT(id, filePath);
	}

	public IInPUT getInput() {
		return input;
	}

	/**
	 * A help-function that simplifies the import of an <code>IInPUT</code> archive on path <code>filePath</code> with id <code>id</code>. 
	 * @param id
	 * @param filePath
	 * @return
	 * @throws InPUTException
	 */
	public static IInPUT getInPUT(String id, String filePath) throws InPUTException {
		synchronized (importer) {
			importer.resetFileName(filePath);
			importer.resetId(id);
			return InPUT.getInPUT(importer);
		}
	}

	/**
	 * Imports the given experiment archive from <code>filePath</code>, giving it id <code>id</code> .
	 * @param id
	 * @param filePath
	 * @return
	 * @throws InPUTException
	 * @throws IOException
	 */
	public IExperiment importExperiment(String id, String filePath) throws InPUTException, IOException {
		IExperiment result = importExperiment(input, id, filePath);
		if (result != null)
			experimentIds.put(id, filePath);
		return result;
	}

	/**
	 * A handle to simply retrieve an empty result output for the <code>IInPUT</code> type investigation, giving it id <code>id</code>.
	 * @param id
	 * @return
	 * @throws InPUTException
	 */
	protected IDesign createNewSettableOutput(String id) throws InPUTException {
		return input.getOutputSpace().nextEmptyDesign(id);
	}

	/**
	 * A simple handle to export or write-back an experiment (with or without output) to the filePath.
	 * @param filePath
	 * @param experiment
	 * @throws InPUTException
	 */
	public static void writeBackExperiment(String filePath, IExperiment experiment) throws InPUTException {
		synchronized (zipExporter) {
			zipExporter.resetFileName(filePath);
			experiment.export(zipExporter);
		}
	}

	/**
	 * A simple handle to add all outputs in <code>outputs</code> to IExperiment <code>experiment</code>.
	 * @param experiment
	 * @param outputs
	 * @throws InPUTException
	 */
	protected void writeBackOutput(IExperiment experiment, List<IDesign> outputs) throws InPUTException {
		String filePath = experimentIds.get(experiment.getId());
		synchronized (zipExporter) {
			zipExporter.resetFileName(filePath);
			for (IDesign output : outputs)
				experiment.addOutput(output);
			experiment.export(zipExporter);
		}
	}

	/**
	 * write back a single IDesign <code>design</code> to the experimental archive <code>experiment</code>.
	 * @param experiment
	 * @param output
	 * @throws InPUTException
	 */
	public void writeBackOutput(IExperiment experiment, IDesign output) throws InPUTException {
		String filePath = experimentIds.get(experiment.getId());
		synchronized (zipExporter) {
			zipExporter.resetFileName(filePath);
			output.attachEnvironmentInfo();
			experiment.addOutput(output);
			experiment.export(zipExporter);
		}
	}

	/**
	 * Is supposed to be implemented by the developer, executing the experiment, and returning the desired result in arbitrary format.
	 * @param experiment
	 * @return
	 * @throws InPUTException
	 */
	public abstract T execute(IExperiment experiment) throws InPUTException;

	/**
	 * Import a given experiment as an archive file in position <code>filePath</code>. The experiment has to be compatible with
	 * the <code>IInPUT</code>.
	 * @param input
	 * @param id
	 * @param filePath
	 * @return
	 * @throws InPUTException
	 * @throws IOException
	 */
	public static IExperiment importExperiment(IInPUT input, String id, String filePath) throws InPUTException, IOException {
		if (!new File(filePath).exists())
			return null;
		synchronized (experimentImporter) {
			experimentImporter.resetFileName(filePath);
			return input.impOrt(id, experimentImporter);
		}
	}
	
	public static int getExistingOutputsFor(IExperiment experiment) {
		return experiment.getOutput().size();
	}
}
