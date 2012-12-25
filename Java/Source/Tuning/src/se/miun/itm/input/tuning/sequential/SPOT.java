package se.miun.itm.input.tuning.sequential;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.PropertiesExporter;
import se.miun.itm.input.impOrt.InputStreamImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.tuning.sequential.spot.SPOTQ;
import se.miun.itm.input.tuning.sequential.spot.SpotHelper;
import se.miun.itm.input.util.Q;

/**
 * The implementation of SPOT for InPUT in Java. This interface simplifies
 * experimentation from within Java. It allows for direct use of all statistical
 * support in R, especially, the use of the sequential optimization and
 * retrieval of improved designs with statistical backup.
 * 
 * TODO in a future version, the configuration of SPOT should be externalized
 * for InPUT. That way, all features can simply be set using InPUT design files.
 * 
 * @author Felix Dobslaw
 * 
 */
public class SPOT extends SequentialTuner {

	enum TunerState {
		INITIAL_DESIGN, AWAITING_INITIAL_RESULTS, SEQUENTIAL_DESIGN, FINISHED;
	}

	private final SpotHelper helper;

	private final IDesign config;

	private TunerState state;

	/** 
	 * TODO create a SPOT that is based on a former experimental image. So either have it as a constructor parameter
	 * or load it by automation and see if an inputConfig is set. If it is set, the state should be adjusted accordingly.
	 * That way experiments that abruptly ended can be resumed. 
	 */
	
	/**
	 * In test stadium: the R environment used is a "one in a JVM thing", so
	 * that only one SPOT at a time can be created in practice.
	 * 
	 * @param input
	 * @throws InPUTException
	 */
	public SPOT(IInPUT input, List<IDesign> problems, String spotConfigPath, String studyId)
			throws InPUTException {
		super(input, problems, studyId);
		config = initConfig(spotConfigPath);
		helper = new SpotHelper(input, config, studyId);
		state = TunerState.INITIAL_DESIGN;
		initSeed();
	}

	private void initSeed() throws InPUTException {
		long seed = config.getValue(SPOTQ.CONF_ALG_SEED);
		setSeed(seed);
	}

	private void setSeed(long seed) throws InPUTException {
		Random rng = InPUTConfig.getValue(Q.RANDOM);
		rng.setSeed(seed);
	}

	/**
	 * In test stadium: the R environment used is a "one in a JVM thing", so
	 * that only one SPOT at a time can be created in practice.
	 * 
	 * @param input
	 * @throws InPUTException
	 */
	public SPOT(IInPUT input, List<IDesign> problems) throws InPUTException {
		this(input, problems, null, null);
	}

	private IDesign initConfig(String spotConfigPath) throws InPUTException {
		InputStreamImporter importer = getCorrectSpotSetupStream(spotConfigPath);

		IDesignSpace configSpace = DesignSpace
				.lookup(SPOTQ.SPOT_DESIGN_SPACE_ID);
		if (configSpace == null)
			configSpace = new DesignSpace(
					SPOTQ.class
							.getResourceAsStream(SPOTQ.SPOT_DESIGN_SPACE_FILE));
		IDesign spotConfig = configSpace.impOrt(importer);

		return spotConfig;
	}

	private InputStreamImporter getCorrectSpotSetupStream(String spotConfigPath) throws InPUTException {
		InputStream is = getCorrectSpotSetup(spotConfigPath); 

		InputStreamImporter importer = new InputStreamImporter(is
				, true);
		return importer;
	}

	private InputStream getCorrectSpotSetup(String spotConfigPath) throws InPUTException {
		InputStream is;
		if (spotConfigPath != null)
		{
			try {
				is = new FileInputStream(spotConfigPath);
			} catch (FileNotFoundException e) {
				throw new InPUTException("There is no spot config file to where you point in \""+ spotConfigPath +"\".",e);
			}
		}
		else
			is = SPOTQ.class.getResourceAsStream(SPOTQ.SPOT_DESIGN_FILE);
		return is;
	}

	public void setProperty(String key, String value) throws InPUTException {
		if (key.equals(SPOTQ.ATTR_SEED))
			setSeed(new Long(value));
		config.setValue(key, value);
	}

	public Object getProperty(String key) throws InPUTException {
		return config.getValue(key);
	}

	public Properties getProperties() throws InPUTException {
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

	private boolean standardRFunction(String key) {
		return key.equals(SPOTQ.CONF_SEQ_TRANSFORMATION)
				|| key.equals(SPOTQ.CONF_SEQ_MERGE_FUNCTION);
	}

	@Override
	public boolean hasNextIteration() {
		boolean hasNext = false;
		switch (state) {
		case INITIAL_DESIGN:
		case AWAITING_INITIAL_RESULTS:
		case SEQUENTIAL_DESIGN:
			hasNext = true;
			break;
		}
		return hasNext;
	}

	@Override
	protected void internalFeedback(List<IExperiment> results)
			throws InPUTException {
		helper.feedbackSpot(results);
		switch (state) {
		case AWAITING_INITIAL_RESULTS:
			state = TunerState.SEQUENTIAL_DESIGN;
			break;
		}
	}

	@Override
	protected List<IExperiment> nextInternalIteration() throws InPUTException {

		if (state == TunerState.INITIAL_DESIGN)
			helper.initExperimentalFolder();
		
		helper.updateConfigurationFile(getProperties());

		if (!hasNextIteration())
			throw new InPUTException(
					"The SPOT experimenter has no designs left for this investigation.");

		List<IExperiment> experiments = null;
		switch (state) {
		case INITIAL_DESIGN:
		case AWAITING_INITIAL_RESULTS:
			experiments = helper.getInitialDesign();
			state = TunerState.AWAITING_INITIAL_RESULTS;
			break;
		case SEQUENTIAL_DESIGN:
			experiments = helper.getSequentialDesign();
			break;
		}

		return experiments;
	}

	@Override
	public void resetStudy(List<IDesign> problems, String studyId) throws InPUTException {
		super.resetStudy(problems, studyId);
		state = TunerState.INITIAL_DESIGN;
		helper.reset(studyId);
	}
	
	public String getExperimentalFolderPath() {
		return helper.getExperimentalFolderPath();
	}
}