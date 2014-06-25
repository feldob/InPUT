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
 */package se.miun.itm.input.tuning.sequential.spot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.export.PropertiesExporter;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.export.ZipFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.converter.SpotConverter;
import se.miun.itm.input.util.InputStreamWrapper;
import se.miun.itm.input.util.Q;

public class SpotHelper {

	private static AtomicInteger globalExperimentCounter = new AtomicInteger(0);

	private static SpotConverter converter;

	static {
		try {
			converter = new SpotConverter();
		} catch (InPUTException e) {
			System.out.println("An internal initialization error of class SpotConverter occured. Is the config.xml valid (esp. the validation setup)?");
		}
	}

	private final SpotROI inputROI;

	private final SpotROI outputROI;
	// TODO change the current design for resumed projects
	private SpotDES currentDES;
	// TODO change the current results for resumed projects
	private final SpotRES currentRES;

	private String[] paramIds;

	private final String investigationId;

	private final File experimentalFolder;

	private final IDesign config;

	private final IInPUT input;

	private String studyId;

	// has ever a result been registered during this run?
	private boolean noResultsSoFar = true;

	private static Rengine engine;

	private static int repetitionCounter;

	static {
		String[] args = { "--vanilla" };
		engine = new Rengine(args, true, null);
		// engine.DEBUG = 1;
		REXP allRight = runCommand(SPOTQ.COMMAND_LOAD_SPOT, false);

		if (allRight == null)
			System.err.println("SPOT is not appropriately installed. Open R and install SPOT by: 'install.packages(\"SPOT\")'.");
	}

	private static REXP runCommand(String command, boolean convert) {
		boolean obtainedLock = engine.getRsync().safeLock();
		REXP value;
		try {
			value = engine.eval(command, convert);
		} finally {
			if (obtainedLock)
				engine.getRsync().unlock();
		}
		if (!convert)
			engine.waitForR();
		return value;
	}

	public SpotHelper(IInPUT input, IDesign config, String studyId, boolean minProblem, List<IDesign> problems, boolean resumeExisting) throws InPUTException {
		experimentalFolder = initExperimentalFolder(studyId, config, problems, resumeExisting);

		this.studyId = experimentalFolder.getName();

		investigationId = initExperimentId(studyId, config, experimentalFolder);
		this.config = initConfig(investigationId, config);

		printInfo(config, minProblem);

		this.input = input;
		initInverseFunction();
		checkSPOTIsInstalled();
		inputROI = new SpotROI(input);
		outputROI = new SpotROI(input.getOutputSpace());
		currentRES = new SpotRES(inputROI, outputROI);

		if (resumeExisting)
			loadSPOTWorkspace();
		else
			initExperimentalFolder();
	}

	private void loadSPOTWorkspace() {
		runCommand("load(paste(getwd(), \"" + studyId + "\", \".RData\", sep = \"" + File.separator + "\"))", false);
	}

	private void printInfo(IDesign config, boolean minProblem) throws InPUTException {
		// Info to be printed so that missunderstandings are avoided.
		System.out.println("The tuning uses the fitness transformation function \"" + config.getValueToString("seq.transformation.func")
				+ "\". The problem is defined as a " + (minProblem ? "minimization" : "maximization") + " problem.");
	}

	private void initResultHeading() {
		runCommand("inputConfig$alg.currentResult <- read.table(textConnection(\"" + currentRES.toString() + "\"), header=TRUE)", false);
	}

	private void checkSPOTIsInstalled() throws InPUTException {

		REXP spotInstalled = runCommand("is.element('SPOT', installed.packages()[,1])", true);
		if (!spotInstalled.asBool().isTRUE())
			throw new InPUTException(
					"In order to use InPUT tuning extension, you have to install the SPOT package for R: \"install.packages('SPOT')\" in the R console.");
	}

	private static IDesign initConfig(String investigationId, IDesign config) throws InPUTException {
		Properties properties = updateProperties(config);
		String filePath = fileId(investigationId, SPOTQ.FILE_CONFIG_ENDING);
		try {
			properties.store(new FileOutputStream(filePath), null);
		} catch (FileNotFoundException e) {
			throw new InPUTException("There is no such file: " + filePath);
		} catch (IOException e) {
			throw new InPUTException("Something went wrong, writing to the file: " + filePath);
		}

		return config;
	}

	public static Properties updateProperties(IDesign config) throws InPUTException {
		Properties prop = config.export(new PropertiesExporter());
		String value;
		for (String key : prop.stringPropertyNames()) {
			value = prop.getProperty(key);
			Object valueO = config.getValue(key);
			if (valueO instanceof Boolean) {
				value = value.toUpperCase();
			} else if (valueO instanceof String && !standardRFunction(key)) {
				value = '\"' + value + '\"';
			}
			prop.setProperty(key, value);
		}
		return prop;
	}

	private static boolean standardRFunction(String key) {
		return key.equals(SPOTQ.CONF_SEQ_TRANSFORMATION) || key.equals(SPOTQ.CONF_SEQ_MERGE_FUNCTION);
	}

	private void initInverseFunction() {
		runCommand("source(textConnection(\"inverse<-function(x){v<-x\n if(x>0)v<-1/x\n return(v)}\"))", false);
	}

	private static File initExperimentalFolder(String studyId, IDesign config, List<IDesign> problems, boolean resumeExperiment) throws InPUTException {

		File experimentalFolder = null;

		String expFolderId = adjustExpFolderId(studyId, config);

		if (!resumeExperiment) {
			experimentalFolder = createExperimentalFolder(problems, experimentalFolder, expFolderId);
		} else
			experimentalFolder = new File(expFolderId);

		return experimentalFolder;
	}

	private static File createExperimentalFolder(List<IDesign> problems, File experimentalFolder, String expFolderId) throws InPUTException {
		if (expFolderId != null) {
			File expFolder;
			synchronized (globalExperimentCounter) { // make sure that the
														// counter is
				// consistent
				expFolder = new File(expFolderId);
				while (expFolder.exists()) {
					repetitionCounter = globalExperimentCounter.incrementAndGet();
					expFolder = new File(initExperimentalFolder(expFolderId));
				}
				repetitionCounter = globalExperimentCounter.intValue();
			}
			expFolder.mkdirs();
			experimentalFolder = expFolder;
		}
		if (problems != null)
			addProblems(experimentalFolder, problems);
		return experimentalFolder;
	}

	private static void addProblems(File experimentalFolder, List<IDesign> problems) throws InPUTException {
		File problemsFolder = new File(experimentalFolder + File.separator + SPOTQ.PROBLEMS_INVESTIGATION_FOLDER);
		problemsFolder.mkdir();

		XMLFileExporter exporter = new XMLFileExporter();
		for (IDesign problem : problems) {
			exporter.resetFileName(problemsFolder.getAbsolutePath() + File.separator + problem.getId() + Q.XML);
			problem.export(exporter);
		}
	}

	private static String initExperimentId(String studyId, IDesign config, File experimentalFolder) throws InPUTException {

		String expId = config.getValue(SPOTQ.ATTR_INPUT_EXPERIMENT_ID);

		if (experimentalFolder != null)
			expId = experimentalFolder.getPath() + File.separator + expId;
		return expId;
	}

	private static String adjustExpFolderId(String studyId, IDesign config) throws InPUTException {
		String expFolderId = config.getValue(SPOTQ.ATTR_INPUT_EXPERIMENTAL_FOLDER);
		if (studyId != null)
			expFolderId = expFolderId + "_" + studyId;

		return expFolderId;
	}

	private static String initExperimentalFolder(String expFolderId) {
		if (repetitionCounter > 0)
			return expFolderId + "(" + repetitionCounter + ")";
		return expFolderId;
	}

	public void createROIFile() throws InPUTException {
		createFile(SPOTQ.FILE_ROI_ENDING, inputROI, investigationId);
	}

	private static void createFile(String ending, SpotExportable<InputStream> exportable, String investigationId) throws InPUTException {
		String fileId = fileId(investigationId, ending);
		try {
			OutputStream fileOut = new FileOutputStream(fileId);

			InputStreamWrapper.fromInputStreamToOutputStream(exportable.export(), fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (FileNotFoundException e) {
			throw new InPUTException("The file " + fileId + " could not be found.", e);
		} catch (IOException e) {
			throw new InPUTException("The file " + fileId + " could not be written.", e);
		}

	}

	private static String fileId(String investigationId, String ending) {
		File file = new File(investigationId + ending);

		if (file.exists())
			return new File("").getAbsolutePath() + File.separator + investigationId + ending;
		else
			return file.getAbsolutePath();
	}

	public void initExperimentalFolder() throws InPUTException {
		saveSPOTContext();
		saveInPUTContext();
	}

	private void saveInPUTContext() throws InPUTException {
		input.export(new ZipFileExporter(experimentalFolder.getPath() + File.separator + SPOTQ.INPUT_FOR_INVESTIGATION));
	}

	private void saveSPOTContext() throws InPUTException {
		config.export(new XMLFileExporter(experimentalFolder.getPath() + File.separator + SPOTQ.SPOT_DESIGN_ID + Q.XML));
	}

	public int initInitialDesign() throws InPUTException {
		createROIFile();
		initSPOTConfFileName();
		initSPOTinitialDesign();
		retrieveNextDesign();
		saveSPOTWorkspace();
		currentDES = initializeDesign();
		return currentDES.size();
	}

	private void saveSPOTWorkspace() throws InPUTException {
		runCommand("save.image(paste(getwd(), \"" + studyId + "\", \".RData\", sep = \"" + File.separator + "\"))", false);
		saveSPOTHistory();
	}

	private void saveSPOTHistory() throws InPUTException {
		runCommand("savehistory(file=paste(getwd(), \"" + studyId + "\",\".Rhistory\", sep = \"" + File.separator + "\"))", false);
	}

	public int initSequentialDesign() throws InPUTException {
		writeResultsToSPOTProjectCache();
		initSPOTSequentialDesign();
		saveSPOTWorkspace();
		currentDES = initializeDesign();
		return currentDES.size();
	}

	private void writeResultsToSPOTProjectCache() {
		runCommand("inputConfig$alg.currentResult <- read.table(textConnection(\"" + currentRES.toString() + "\"), header=TRUE)", false);
	}

	public void retrieveNextDesign() {
		REXP expr = runCommand("colnames(inputConfig$alg.currentDesign)", true);
		paramIds = expr.asStringArray();
	}

	public void initSPOTinitialDesign() {
		initInverseFunction();
		runCommand("inputConfig<-spot(inputFile,\"init\")", false);
	}

	public void initSPOTConfFileName() {
		runCommand("inputFile=paste(getwd(), \"" + investigationId + ".conf\", sep = \"" + File.separator + "\")", false);
	}

	public SpotDES initializeDesign() throws InPUTException {
		REXP designs = runCommand("inputConfig$alg.currentDesign", true);
		return new SpotDES(designs.asVector(), paramIds, inputROI);
	}

	public void initSPOTSequentialDesign() {
		String command = "inputConfig<-spot(inputFile,\"seq\", spotConfig=inputConfig)";
		runCommand(command, false);
	}

	public void feedbackSpot(IDesign result) throws InPUTException {
		if (noResultsSoFar) {
			String fileId = fileId(investigationId, SPOTQ.FILE_RES_ENDING);
			if (!new File(fileId).exists())
				createFile(SPOTQ.FILE_RES_ENDING, currentRES, investigationId);
			noResultsSoFar = false;
		}
		SpotResult spotResult = currentRES.append(result, currentDES);

		if (isFileMode())
			feedbackResultInRESFile(spotResult);

		saveSPOTWorkspace();
	}

	private void feedbackResultInRESFile(SpotResult result) throws InPUTException {
		appendToFile(SPOTQ.FILE_RES_ENDING, result, investigationId);
	}

	private static void appendToFile(String ending, SpotResult result, String investigationId) throws InPUTException {
		String fileId = fileId(investigationId, ending);

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileId, true)));
			out.print(result.toString());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			throw new InPUTException("The file " + fileId + " could not be found.", e);
		} catch (IOException e) {
			throw new InPUTException("The file " + fileId + " could not be written.", e);
		}
	}

	private Boolean isFileMode() throws InPUTException {
		return config.getValue(SPOTQ.CONF_IS_FILE_MODE);
	}

	public void reset(String studyId) throws InPUTException {
		this.studyId = studyId;
		runCommand("rm(list=ls())", false);
	}

	public String getExperimentalFolderPath() {
		return experimentalFolder.getPath();
	}

	public IExperiment nextExperiment(int position) throws InPUTException {
		synchronized (converter) {
			if (currentDES == null)
				emulateNextDesign();
			return converter.toExperiment(input.getId(), currentDES, position);
		}
	}

	public int emulateNextDesign() throws InPUTException {
		retrieveNextDesign();
		currentDES = initializeDesign();
		return currentDES.size();
	}
}
