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
 */package se.miun.itm.input.tuning.sequential;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUTConfig;
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

	private final SpotHelper helper;

	private final IDesign config;

	public SPOT(IInPUT input, List<IDesign> problems, String spotConfigPath, String studyId, boolean minProblem, boolean resumeExisting,
			boolean randomProblemChoice) throws InPUTException {
		super(input, problems, studyId, minProblem, randomProblemChoice);

		config = initConfig(spotConfigPath);
		helper = new SpotHelper(input, config, studyId, minProblem, problems, resumeExisting);
		initSeed();
		currentDesignSize = getTotalAmountRunsInitialDesign();
	}

	private void initSeed() throws InPUTException {
		long seed = config.getValue(SPOTQ.CONF_ALG_SEED);
		setSeed(seed);
	}

	private void setSeed(long seed) throws InPUTException {
		Random rng = InPUTConfig.getValue(Q.RANDOM);
		rng.setSeed(seed);
	}

	private IDesign initConfig(String spotConfigPath) throws InPUTException {
		InputStreamImporter importer = getCorrectSpotSetupStream(spotConfigPath);

		IDesignSpace configSpace = DesignSpace.lookup(SPOTQ.SPOT_DESIGN_SPACE_ID);
		if (configSpace == null)
			configSpace = new DesignSpace(SPOTQ.class.getResourceAsStream(SPOTQ.SPOT_DESIGN_SPACE_FILE));
		IDesign spotConfig = configSpace.impOrt(importer);

		return spotConfig;
	}

	private InputStreamImporter getCorrectSpotSetupStream(String spotConfigPath) throws InPUTException {
		InputStream is = getCorrectSpotSetup(spotConfigPath);

		InputStreamImporter importer = new InputStreamImporter(is);
		return importer;
	}

	private InputStream getCorrectSpotSetup(String spotConfigPath) throws InPUTException {
		InputStream is;
		if (spotConfigPath != null) {
			try {
				is = new FileInputStream(spotConfigPath);
			} catch (FileNotFoundException e) {
				throw new InPUTException("There is no spot config file to where you point in \"" + spotConfigPath + "\".", e);
			}
		} else
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

	@Override
	protected void feedback(IExperiment experiment, IDesign newResult) throws InPUTException {
		experiment.addOutput(newResult);
		helper.feedbackSpot(newResult);
	}

	@Override
	protected IExperiment nextExperiment(int position) throws InPUTException {
		return helper.nextExperiment(position);
	}

	@Override
	public void resetStudy(List<IDesign> problems, String studyId) throws InPUTException {
		super.resetStudy(problems, studyId);
		helper.reset(studyId);
	}

	/**
	 * retrieves the folder in which the experimental data is stored. Returns
	 * null if no data is stored.
	 * 
	 * @return
	 */
	public String getExperimentalFolderPath() {
		return helper.getExperimentalFolderPath();
	}

	@Override
	int initNextDesign() throws InPUTException {
		if (getAmountEvaluatedRuns() == 0)
			return helper.initInitialDesign();
		else
			return helper.initSequentialDesign();
	}

	@Override
	public int getTotalAmountRunsInitialDesign() throws InPUTException {
		int first = (Integer) config.getValue(SPOTQ.CONF_INIT_AMOUNT_INVESTIGATED_DESIGNS);
		int second = (Integer) config.getValue(SPOTQ.CONF_INIT_REPEATS_PER_DESIGN);
		return first * second;
	}

	@Override
	public int emulateNextDesignAndReturnSize() {
		try {
			return helper.emulateNextDesign();
		} catch (InPUTException e) {
			throw new IllegalStateException(
					"something went wrong with the emulation of the existing experiment. Maybe something is broken in the experimental structure.", e);
		}
	}
}