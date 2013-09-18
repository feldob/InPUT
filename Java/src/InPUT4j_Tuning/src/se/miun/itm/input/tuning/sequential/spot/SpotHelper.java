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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class SpotHelper {

	private static AtomicInteger globalExperimentCounter = new AtomicInteger(0);

	private static SpotConverter converter;

	static{
		try {
			converter = new SpotConverter();
		} catch (InPUTException e) {
			System.out.println("An internal initialization error of class SpotConverter occured. Is the config.xml valid (esp. the validation setup)?");
		}
	}
	
	private final SpotROI inputROI;

	private final SpotROI outputROI;

	private SpotDES currentDES;

	private final SpotRES currentRES;

	private String[] paramIds;

	private final String investigationId;

	private final File experimentalFolder;

	private final IDesign config;

	private final IInPUT input;

	private String studyId; // TODO does not work for more than one SPOT at a
							// time! Maybe this engine
	// can simply be shared in a meaningful way, using intrinsic locking among
	// all SPOT instances.
	private static Rengine engine;

	private static int repetitionCounter;

	static {
		String[] args = { "--vanilla" };
		engine = new Rengine(args, false, null);
//		engine.DEBUG = 1;
		REXP allRight = engine.eval(SPOTQ.COMMAND_LOAD_SPOT, false);
		if (allRight == null)
			System.err.println("SPOT is not appropriately installed. Open R and install SPOT by: 'install.packages(\"SPOT\")'.");
	}

	public SpotHelper(IInPUT input, IDesign config, String studyId)
			throws InPUTException {
		this.studyId = studyId;
		experimentalFolder = initExperimentalFolder(studyId, config);
		investigationId = initExperimentId(studyId, config, experimentalFolder);
		this.config = initConfig(investigationId, config);
		this.input = input;
		initInverseFunction();
		checkSPOTIsInstalled();
		inputROI = new SpotROI(input);
		outputROI = new SpotROI(input.getOutputSpace());
		currentRES = new SpotRES(inputROI, outputROI);
		initExperimentalFolder();
	}

	private void initResultHeading() {
		engine.eval("inputConfig$alg.currentResult <- read.table(textConnection(\"" + currentRES.toString() + "\"), header=TRUE)");
	}

	private void checkSPOTIsInstalled() throws InPUTException {
		REXP spotInstalled = engine
				.eval("is.element('SPOT', installed.packages()[,1])");
		if (!spotInstalled.asBool().isTRUE())
			throw new InPUTException(
					"In order to use InPUT tuning extension, you have to install the SPOT package for R: \"install.packages('SPOT')\" in the R console.");
	}

	private static IDesign initConfig(String investigationId, IDesign config)
			throws InPUTException {
		Properties properties = updateProperties(config);
		String filePath = fileId(investigationId, SPOTQ.FILE_CONFIG_ENDING);
		try {
			properties.store(new FileOutputStream(filePath), null);
		} catch (FileNotFoundException e) {
			throw new InPUTException("There is no such file: " + filePath);
		} catch (IOException e) {
			throw new InPUTException(
					"Something went wrong, writing to the file: " + filePath);
		}
		return config;
	}

	public static Properties updateProperties(IDesign config)
			throws InPUTException {
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
		return key.equals(SPOTQ.CONF_SEQ_TRANSFORMATION)
				|| key.equals(SPOTQ.CONF_SEQ_MERGE_FUNCTION);
	}

	private void initInverseFunction() {
		engine.eval(
				"source(textConnection(\"inverse<-function(x){v<-x\n if(x>0)v<-1/x\n return(v)}\"))",
				false);
	}

	public static String initRelativeFileString(String studyId, IDesign config,
			String fileName) throws InPUTException {

		String folderId = adjustExpFolderId(studyId, config);

		if (folderId == null || folderId.equals(""))
			return fileName;
		return folderId + repetitionCounter + File.separator + fileName;
	}

	private static File initExperimentalFolder(String studyId, IDesign config)
			throws InPUTException {

		File experimentalFolder = null;

		String expFolderId = adjustExpFolderId(studyId, config);

		if (expFolderId != null) {
			File expFolder;
			synchronized (globalExperimentCounter) { // make sure that the
														// counter is
				// consistent
				expFolder = new File(expFolderId);
				while (expFolder.exists()) {
					repetitionCounter = globalExperimentCounter
							.incrementAndGet();
					expFolder = new File(initExperimentalFolder(expFolderId));
				}
				repetitionCounter = globalExperimentCounter.intValue();
			}
			expFolder.mkdirs();
			experimentalFolder = expFolder;
		}
		return experimentalFolder;
	}

	private static String initExperimentId(String studyId, IDesign config,
			File experimentalFolder) throws InPUTException {

		String expId = config.getValue(SPOTQ.ATTR_INPUT_EXPERIMENT_ID);

		if (experimentalFolder != null)
			expId = experimentalFolder.getPath() + File.separator + expId;
		return expId;
	}

	private static String adjustExpFolderId(String studyId, IDesign config)
			throws InPUTException {
		String expFolderId = config
				.getValue(SPOTQ.ATTR_INPUT_EXPERIMENTAL_FOLDER);
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

	private static void createFile(String ending,
			SpotExportable<InputStream> exportable, String investigationId)
			throws InPUTException {
		String fileId = fileId(investigationId, ending);
		try {
			OutputStream fileOut = new FileOutputStream(fileId);

			InputStreamWrapper.fromInputStreamToOutputStream(
					exportable.export(), fileOut);
			fileOut.flush();
			fileOut.close();
		} catch (FileNotFoundException e) {
			throw new InPUTException("The file " + fileId
					+ " could not be found.", e);
		} catch (IOException e) {
			throw new InPUTException("The file " + fileId
					+ " could not be written.", e);
		}

	}

	private static String fileId(String investigationId, String ending) {
		File file = new File(investigationId + ending);

		if (file.exists())
			return new File("").getAbsolutePath() + File.separator
					+ investigationId + ending;
		else
			return file.getAbsolutePath();
	}

	public void initExperimentalFolder() throws InPUTException {
		saveSPOTContext();
		saveInPUTContext();
	}

	private void saveInPUTContext() throws InPUTException {
		input.export(new ZipFileExporter(experimentalFolder.getPath()
				+ File.separator + "expScope.inp"));
	}

	private void saveSPOTContext() throws InPUTException {
		config.export(new XMLFileExporter(experimentalFolder.getPath()
				+ File.separator + "spotConfig.xml"));
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
		// engine.eval("load(paste(getwd(), \"" + rData + "\", sep = \""
		// + File.separator + "\"))", false);
		String rData = initRelativeFileString(studyId, config, ".RData");
		engine.eval("save.image(paste(getwd(), \"" + rData + "\", sep = \""
				+ File.separator + "\"))", false);
		saveSPOTHistory();
	}

	// TODO does not work!
	private void saveSPOTHistory() throws InPUTException {
		// engine.eval("loadhistory(file=paste(getwd(), \"" + rHistory +
		// "\", sep = \""
		// + File.separator + "\"))", false);
		String rHistory = initRelativeFileString(studyId, config, ".Rhistory");
		engine.eval("savehistory(file=paste(getwd(), \"" + rHistory
				+ "\", sep = \"" + File.separator + "\"))", false);
	}

	public int initSequentialDesign() throws InPUTException {
		writeResultsToSPOTProjectCache();
		initSPOTSequentialDesign();
		saveSPOTWorkspace();
		currentDES = initializeDesign();
		return currentDES.size();
	}

	private void writeResultsToSPOTProjectCache() {
		engine.eval("inputConfig$alg.currentResult <- read.table(textConnection(\"" + currentRES.toString() + "\"), header=TRUE)");
	}

	public void retrieveNextDesign() {
		paramIds = engine.eval("colnames(inputConfig$alg.currentDesign)")
				.asStringArray();
	}

	public void initSPOTinitialDesign() {
		initInverseFunction();
		engine.eval("inputConfig<-spot(inputFile,\"init\")", false);
	}

	public void initSPOTConfFileName() {
		engine.eval("inputFile=paste(getwd(), \"" + investigationId
				+ ".conf\", sep = \"" + File.separator + "\")", false);
	}

	public SpotDES initializeDesign() throws InPUTException {
		REXP designs = engine.eval("inputConfig$alg.currentDesign");
		return new SpotDES(designs.asVector(), paramIds, inputROI);
	}

	public void initSPOTSequentialDesign() {
		engine.eval(
				"inputConfig<-spot(inputFile,\"seq\", spotConfig=inputConfig)",
				false);
	}

	public void feedbackSpot(IDesign result) throws InPUTException {
		currentRES.append(result, currentDES);
		
		if (isFileMode())
			feedbackResultInRESFile();

		saveSPOTWorkspace();
	}

	private void feedbackResultInRESFile() throws InPUTException {
			createFile(SPOTQ.FILE_RES_ENDING, currentRES, investigationId);
	}

	private Boolean isFileMode() throws InPUTException {
		return config.getValue(SPOTQ.CONF_IS_FILE_MODE);
	}

	public void reset(String studyId) throws InPUTException {
		this.studyId = studyId;
		engine.eval("rm(list=ls())", false);
	}

	public String getExperimentalFolderPath() {
		return experimentalFolder.getPath();
	}

	public IExperiment nextExperiment(int position) throws InPUTException {
		synchronized (converter) {
			return converter.toExperiment(input.getId(), currentDES, position);
		}
	}
}
