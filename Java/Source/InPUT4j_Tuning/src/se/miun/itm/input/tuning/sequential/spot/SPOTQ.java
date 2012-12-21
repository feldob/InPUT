package se.miun.itm.input.tuning.sequential.spot;

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
