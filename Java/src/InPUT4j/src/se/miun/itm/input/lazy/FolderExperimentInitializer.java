package se.miun.itm.input.lazy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Stack;

import se.miun.itm.input.ExperimentConductor;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.design.IDesign;

public class FolderExperimentInitializer implements LazyExperimentInitializer {

	private class ExperimentFileFilter implements FilenameFilter {

		private boolean includeAlreadyRun = false;

		@Override
		public boolean accept(File dir, String name) {
			boolean accept = true;
			if (!isExperiment(name)){
				accept = false;
			}else
				if (!includeAlreadyRun)
					accept = !hasAlreadyBeenRun(name);
			return accept;
		}

		private boolean hasAlreadyBeenRun(String name) {
			return name.contains("_mean");
		}

		private boolean isExperiment(String name) {
			return name.endsWith(".exp") && !name.contains("prrReference");
		}

		public void setIncludeThoseWhoAlkreadyHaveBeenRun(boolean include) {
			this.includeAlreadyRun = include;
		}

	}

	private final Stack<File> experimentalFiles;

	private final ExperimentFileFilter experimentFilter;

	private final ExperimentConductor<IDesign> conductor;

	public FolderExperimentInitializer(String folderPath,
			ExperimentConductor<IDesign> conductor) {
		this.conductor = conductor;
		experimentFilter = new ExperimentFileFilter();
		experimentalFiles = initExperimentsExcludingThoseWhoAlreadyHaveBeenRun(folderPath);
	}
	
	private Stack<File> initExperimentsExcludingThoseWhoAlreadyHaveBeenRun(
			String folderPath) {
		return initExperiments(folderPath, false);
	}

	private Stack<File> initExperiments(String folderPath, boolean includeAlreadyRun) {
		File parentFolder = initFolder(folderPath);
		experimentFilter.setIncludeThoseWhoAlkreadyHaveBeenRun(includeAlreadyRun);
		File[] experimentFiles = parentFolder.listFiles(experimentFilter);
		return convertExperimentalFilesToStack(experimentFiles);
	}

	private Stack<File> convertExperimentalFilesToStack(File[] experimentFiles) {
		Stack<File> stack = new Stack<File>();
		for (File experimentalFile : experimentFiles)
			stack.add(experimentalFile);
		return stack;
	}

	private File initFolder(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists())
			throw new IllegalArgumentException("There exists no folder: '"
					+ folderPath + "'.");
		return folder;
	}

	@Override
	public boolean hasNext() {
		return !experimentalFiles.isEmpty();
	}

	@Override
	public void remove() {
		experimentalFiles.remove(0);
	}

	@Override
	public IExperiment next() {
		File experimentFile = experimentalFiles.pop();
		String experimentId = experimentFile.getName();
		IExperiment experiment = initExperiment(experimentFile, experimentId);
		return experiment;
	}

	private IExperiment initExperiment(File experimentFile, String experimentId) {
		try {
			return conductor.importExperiment(experimentId,
					experimentFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int size() {
		return experimentalFiles.size();
	}
}