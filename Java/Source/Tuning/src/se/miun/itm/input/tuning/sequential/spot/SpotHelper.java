package se.miun.itm.input.tuning.sequential.spot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.export.XMLFileExporter;
import se.miun.itm.input.export.ZipFileExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.converter.SpotConverter;
import se.miun.itm.input.util.InputStreamWrapper;

public class SpotHelper {

	private static AtomicInteger globalExperimentCounter = new AtomicInteger(0);

	// @NotThreadSafe
	private static final SpotConverter converter = new SpotConverter();

	private final SpotROI inputROI;

	private final SpotROI outputROI;

	private SpotDES currentDES;

	private SpotRES currentRES;

	private String[] paramIds;

	private String investigationId;

	private String experimentalFolder;

	private final IDesign config;

	private final IInPUT input;

	private String studyId;

	// TODO does not work for more than one SPOT at a time! Maybe this engine
	// can simply be shared in a meaningful way, using intrinsic locking among
	// all SPOT instances.
	private static Rengine engine;

	private static int repetitionCounter;

	static {
		String[] args = { "--vanilla" };
		engine = new Rengine(args, false, null);
		engine.eval(SPOTQ.COMMAND_LOAD_SPOT, false);
	}

	public SpotHelper(IInPUT input, IDesign config, String studyId)
			throws InPUTException {
		this.studyId = studyId;
		this.config = config;
		this.input = input;
		initInverseFunction();
		inputROI = new SpotROI(input);
		outputROI = new SpotROI(input.getOutputSpace());
	}

	private void initInverseFunction() {
		engine.eval("source(textConnection(\"inverse<-function(x){v<-x\n if(x>0)v<-1/x\n return(v)}\"))", false);
	}

	public String initRelativeFileString(String fileName)
			throws InPUTException {

		String folderId = adjustExpFolderId();

		if (folderId == null || folderId.equals(""))
			return fileName;
		return folderId + repetitionCounter + File.separator + fileName;
	}

	private String prepareAndCreateId() throws InPUTException {

		String expFolderId = adjustExpFolderId();
		String expId = config.getValue(SPOTQ.ATTR_INPUT_EXPERIMENT_ID);

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
			experimentalFolder = expFolder.getPath();
			expId = initExperimentalId(expFolderId, expId);
		}
		return expId;
	}

	private String adjustExpFolderId() throws InPUTException {
		String expFolderId = config
				.getValue(SPOTQ.ATTR_INPUT_EXPERIMENTAL_FOLDER);
		if (studyId != null)
			expFolderId = expFolderId + "_" + studyId;

		return expFolderId;
	}

	private String initExperimentalId(String expFolderId, String expId) {
		expFolderId = initExperimentalFolder(expFolderId);
		return expFolderId + File.separator + expId;
	}

	private String initExperimentalFolder(String expFolderId) {
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
		investigationId = prepareAndCreateId();
		saveSPOTContext();
		saveInPUTContext();
	}

	private void saveInPUTContext() throws InPUTException {
		input.export(new ZipFileExporter(adjustExpFolderId() + File.separator + "expScope.inp"));
	}

	private void saveSPOTContext() throws InPUTException {
		config.export(new XMLFileExporter(adjustExpFolderId() + File.separator + "spotConfig.xml"));
	}

	public List<IExperiment> getInitialDesign() throws InPUTException {
		createROIFile();
		initSPOTConfFileName();
		initSPOTinitialDesign();
		retrieveNextDesign();
		saveSPOTWorkspace();
		return initializeDesign();
	}

	private void saveSPOTWorkspace() throws InPUTException {
		// engine.eval("load(paste(getwd(), \"" + rData + "\", sep = \""
		// + File.separator + "\"))", false);
		String rData = initRelativeFileString(".RData");
		engine.eval("save.image(paste(getwd(), \"" + rData + "\", sep = \""
				+ File.separator + "\"))", false);
		saveSPOTHistory();
	}

	// TODO does not work!
	private void saveSPOTHistory() throws InPUTException {
		// engine.eval("loadhistory(file=paste(getwd(), \"" + rHistory +
		// "\", sep = \""
		// + File.separator + "\"))", false);
		String rHistory = initRelativeFileString(".Rhistory");
		engine.eval("savehistory(file=paste(getwd(), \"" + rHistory
				+ "\", sep = \"" + File.separator + "\"))", false);
	}

	public List<IExperiment> getSequentialDesign() throws InPUTException {
		initSPOTSequentialDesign();
		saveSPOTWorkspace();
		return initializeDesign();
	}

	public void retrieveNextDesign() {
		paramIds = engine.eval("colnames(inputConfig$alg.currentDesign)")
				.asStringArray();
	}

	public void initSPOTinitialDesign() {
		initInverseFunction();
		engine.eval("inputConfig=spot(inputFile,\"init\")", false);
	}

	public void initSPOTConfFileName() {
		engine.eval("inputFile=paste(getwd(), \"" + investigationId
				+ ".conf\", sep = \"" + File.separator + "\")", false);
	}

	public List<IExperiment> initializeDesign() throws InPUTException {
		REXP designs = engine.eval("inputConfig$alg.currentDesign");

		currentDES = new SpotDES(designs.asVector(), paramIds, inputROI);
		return converter.toExperiments(input.getId(), currentDES);
	}

	public void initSPOTSequentialDesign() {
		engine.eval(
				"inputConfig=spot(inputFile,\"seq\", spotConfig=inputConfig)",
				false);
	}

	public void feedbackSpot(List<IExperiment> results) throws InPUTException {

		if (currentRES == null)
			currentRES = new SpotRES(results, currentDES, inputROI, outputROI);
		else
			currentRES.append(results, currentDES);
		feedbackSpotInMemory();
		saveSPOTWorkspace();
	}

	private void feedbackSpotInMemory() throws InPUTException {
		StringBuilder res = currentRES.toSpot();
		engine.eval("inputConfig$alg.currentResult <- rbind(inputConfig$alg.currentResult, read.table(textConnection(\""
				+ res.toString() + "\"), header=TRUE))");
		if (config.getValue(SPOTQ.CONF_IS_FILE_MODE))
			createFile(SPOTQ.FILE_RES_ENDING, currentRES, investigationId);
	}

	public void updateConfigurationFile(Properties properties)
			throws InPUTException {
		String filePath = fileId(investigationId, SPOTQ.FILE_CONFIG_ENDING);
		try {
			properties.store(new FileOutputStream(filePath), null);
		} catch (FileNotFoundException e) {
			throw new InPUTException("There is no such file: " + filePath);
		} catch (IOException e) {
			throw new InPUTException(
					"Something went wrong, writing to the file: " + filePath);
		}
	}

	public void reset(String studyId) throws InPUTException {
		this.studyId = studyId;
		engine.eval("rm(list=ls())", false);
	}

	public String getExperimentalFolderPath() {
		return experimentalFolder;
	}
}
