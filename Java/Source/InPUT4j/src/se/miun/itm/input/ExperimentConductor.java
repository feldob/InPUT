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

public abstract class ExperimentConductor<T> {

	private static final InPUTArchiveImporter importer = new InPUTArchiveImporter();

	private static final ExperimentArchiveImporter experimentImporter = new ExperimentArchiveImporter();

	private static final ZipFileExporter zipExporter = new ZipFileExporter();

	private final Map<String, String> experimentIds = new HashMap<String, String>();

	private IInPUT input;

	public ExperimentConductor(IInPUT input) {
		this.input = input;
	}

	public ExperimentConductor(String id, String filePath) throws InPUTException {
		this.input = getInPUT(id, filePath);
	}

	public IInPUT getInput() {
		return input;
	}

	public static IInPUT getInPUT(String id, String filePath) throws InPUTException {
		synchronized (importer) {
			importer.resetFileName(filePath);
			importer.resetId(id);
			return InPUT.getInPUT(importer);
		}
	}

	public IExperiment initExperiment(String id, String filePath) throws InPUTException, IOException {
		experimentIds.put(id, filePath);
		return initExperiment(input, id, filePath);
	}

	protected IDesign createNewSettableOutput(String id) throws InPUTException {
		return input.getOutputSpace().nextEmptyDesign(id);
	}

	public static void writeBackExperiment(String filePath, IExperiment experiment) throws InPUTException {
		synchronized (zipExporter) {
			zipExporter.resetFileName(filePath);
			experiment.export(zipExporter);
		}
	}

	protected void writeBackOutput(IExperiment experiment, List<IDesign> outputs) throws InPUTException {
		String filePath = experimentIds.get(experiment.getId());
		synchronized (zipExporter) {
			zipExporter.resetFileName(filePath);
			for (IDesign output : outputs)
				experiment.addOutput(output);
			experiment.export(zipExporter);
		}
	}

	protected void writeBackOutput(IExperiment experiment, IDesign output) throws InPUTException {
		String filePath = experimentIds.get(experiment.getId());
		synchronized (zipExporter) {
			zipExporter.resetFileName(filePath);
			output.attachEnvironmentInfo();
			experiment.addOutput(output);
			experiment.export(zipExporter);
		}
	}

	public abstract T execute(IExperiment experiment) throws InPUTException;

	public static IExperiment initExperiment(IInPUT input, String id, String filePath) throws InPUTException, IOException {
		if (!new File(filePath).exists())
			return null;
		synchronized (experimentImporter) {
			experimentImporter.resetFileName(filePath);
			return input.impOrt(id, experimentImporter);
		}
	}
}