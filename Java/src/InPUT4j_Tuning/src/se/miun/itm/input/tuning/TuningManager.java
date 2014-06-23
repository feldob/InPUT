package se.miun.itm.input.tuning;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import se.miun.itm.input.ExperimentConductor;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.sequential.SPOT;
import se.miun.itm.input.tuning.sequential.spot.SPOTQ;
import se.miun.itm.input.util.ProblemTopologyUtil;
import se.miun.itm.input.util.Q;

/**
 * A simple standard interface for the conduct of tuning series.
 * It has to be inherited by a specific tuner and requires the
 * definition of a ExperimentConductor for the investigation call.
 * 
 * @author feldob
 * 
 */
public abstract class TuningManager<T> {

	private final IInPUT roi;

	private final List<IDesign> problems;

	private final String investigationPath;

	private static final ExperimentArchiveImporter importer = new ExperimentArchiveImporter();

	protected int experimentCounter = 0;

	public TuningManager(String investigationPath) throws InPUTException, IOException {
		this.investigationPath = investigationPath;
		roi = initInPUTfromExistingInvestigation(investigationPath);
		problems = ProblemTopologyUtil.importProblemInstances(roi, investigationPath + File.separator + SPOTQ.PROBLEMS_INVESTIGATION_FOLDER);
	}

	public TuningManager(String investigationId, String inputArchivePath, String problemInstanceFolderPath) throws InPUTException, IOException {
		investigationPath = null;
		roi = ExperimentConductor.getInPUT(investigationId, inputArchivePath);
		problems = ProblemTopologyUtil.importProblemInstances(roi, problemInstanceFolderPath);
	}

	private IInPUT initInPUTfromExistingInvestigation(String investigationPath) throws InPUTException {
		File investigationDirectory = new File(investigationPath);
		String investigationId = investigationDirectory.getName();
		investigationId = investigationId.split(Pattern.quote("_"))[1];
		String inputArchivePath = investigationDirectory.getAbsolutePath() + File.separator + SPOTQ.INPUT_FOR_INVESTIGATION;
		return ExperimentConductor.getInPUT(investigationId, inputArchivePath);
	}

	/**
	 * only for existing investigations that should be proceeded. uses the
	 * existing config file.
	 */
	public void resumeInvestigation(ExperimentConductor<T> conductor) throws InPUTException, IOException {
		if (investigationPath == null)
			throw new IllegalStateException("There is no existing investigation path, which means that it cannot be proceeded.");

		String spotConfigPath = investigationPath + File.separator + SPOTQ.SPOT_DESIGN_ID + Q.XML;

		investigate(spotConfigPath, true, conductor);
	}

	private void investigate(String spotConfigPath, boolean resumeExisting, ExperimentConductor<T> conductor) throws InPUTException, IOException {
		SPOT tuner = new SPOT(roi, problems, spotConfigPath, roi.getId(), true, resumeExisting);
		System.out.println("Investigation started ...");

		String experimentFolder = tuner.getExperimentalFolderPath() + File.separator;
		int runsInitialDesign = tuner.getTotalAmountRunsInitialDesign();

		System.out.println("\tInitial Design:");
		String experimentPath;
		for (int i = 0; i < runsInitialDesign; i++) {
			experimentPath = experimentFolder + ++experimentCounter + Q.EXP;
			if (resumeExisting && new File(experimentPath).exists()) {
				System.out.println("		Skip Experiment nr." + (i + 1) + "/" + tuner.currentDesignSize());
				importer.resetFileName(experimentPath);
				IExperiment experiment = roi.impOrt("" + (i + 1), importer);
				emulateNextExperiment(tuner, experiment);
				continue;
			}
			System.out.println("\t\tExperiment nr." + (i + 1) + "/" + tuner.currentDesignSize());
			executeNextExperiment(tuner, conductor, experimentFolder);
		}

		int sequentialExperimentCounter = 0;
		while (true) {
			sequentialExperimentCounter++;
			System.out.println("\tSequential Design " + sequentialExperimentCounter + ":");
			for (int i = 0; i < tuner.currentDesignSize(); i++) {
				experimentPath = experimentFolder + ++experimentCounter + Q.EXP;
				if (resumeExisting && new File(experimentPath).exists()) {
					System.out.println("\t\tSkip Experiment nr." + (i + 1) + "/" + tuner.currentDesignSize());
					importer.resetFileName(experimentPath);
					IExperiment experiment = roi.impOrt("" + (i + 1), importer);
					emulateNextExperiment(tuner, experiment);
					continue;
				}
				System.out.println("\t\tExperiment nr." + (i + 1) + "/" + tuner.currentDesignSize());
				executeNextExperiment(tuner, conductor, experimentFolder);
			}
		}
	}

	private void emulateNextExperiment(SPOT tuner, IExperiment experiment) throws InPUTException {
		tuner.emulateNextExperiment(experiment);
	}

	private void executeNextExperiment(SPOT tuner, ExperimentConductor<T> conductor, String experimentFolder) throws InPUTException {
		IExperiment experiment = tuner.nextExperiment();
		System.out.println("\t\t\t\tProblem Instance nr. " + experiment.getProblemFeatures().getId());
		T resStats = conductor.execute(experiment);
		IDesign result = extractResultsFrom(resStats);
		tuner.feedback(result);
		ExperimentConductor.writeBackExperiment(experimentFolder + experimentCounter, experiment);
	}

	protected abstract IDesign extractResultsFrom(T resStats);

	public void investigate(String spotConfigPath, ExperimentConductor<T> conductor) throws InPUTException, IOException {
		investigate(spotConfigPath, false, conductor);
	}

	public IInPUT getROI() {
		return roi;
	}
}
