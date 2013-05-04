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

public class SPOTQ {


	public static final String CONF_IS_FILE_MODE = "spot.fileMode";

	public static final String CONF_SPOT_SEED = "spot.seed";

	public static final String CONF_SEQ_PREDICTION_MODEL = "seq.predictionModel.func";

	public static final String CONF_SEQ_AMOUNT_INVESTIGATED_DESIGNS = "seq.design.new.size";

	public static final String CONF_INIT_REPEATS_PER_DESIGN = "init.design.repeats";

	public static final String CONF_INIT_AMOUNT_INVESTIGATED_DESIGNS = "init.design.size";

	public static final String CONF_INIT_MODEL = "init.design.func";

	public static final String CONF_ALG_SEED = "alg.seed";
	
	public static final String CONF_SEQ_TRANSFORMATION = "seq.transformation.func";

	public static final String CONF_SEQ_MERGE_FUNCTION= "seq.merge.func";


	
	public static final String ATTR_ALGORITHM_ID = "FUNCTION";

	public static final String ATTR_AMOUNT_DIMENSIONS = "DIM";

	public static final String ATTR_ITERATION = "STEP";

	public static final String ATTR_CONFIGURATION_NUMBER = "CONFIG";

	public static final String ATTR_SEED = "SEED";

	
	
	
	public static final String FILE_ROI_ENDING = ".roi";

	public static final String FILE_CONFIG_ENDING = ".conf";
	
	public static final String FILE_RES_ENDING = ".res";
	
	
	
	public static final String COMMAND_LOAD_SPOT = "library(SPOT)";

	public static final String SPOT_DESIGN_SPACE_FILE = "spotSpace.xml";
	public static final String SPOT_DESIGN_SPACE_ID = "spot";

	public static final String SPOT_DESIGN_FILE = "spot.xml";
	public static final String SPOT_DESIGN_ID = "spotConfig";
		
	
	
	public static final String ATTR_INPUT_EXPERIMENTAL_FOLDER = "spot.folder";

	public static final String ATTR_INPUT_EXPERIMENT_ID = "alg.id";

	public static final String Y = "Y";

	public static final String NULL = "NULL";
}
