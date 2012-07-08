package se.miun.itm.input.log.model;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.param.Param;

public aspect DesignSpaceLogger {

	private static Logger log = LoggerFactory.getLogger("space");

	pointcut setRandom(Design design, Map<String, Object> vars,
			Param param) : execution(private void DesignSpace.setRandom(Design, Map<String, Object>,ParamElement)) && args(design, vars, param) && if(InPUTConfig.isLoggingActive());

	pointcut nextDesign(String designId) : execution(public IDesign DesignSpace.nextDesign(String)) && args(designId) && if(InPUTConfig.isLoggingActive());

	before(String designId) : nextDesign(designId) {
		log.info("Randomly (re)instantiate design '" + designId + "'.");
	}

	before(DesignSpace space, Design design, Map<String, Object> vars,
			Param param) : setRandom(design, vars, param) && target(space) {
		log.info("Randomly (re)instantiate space '" + space.getId() 
				+ "' for instance '" + design.getId() + "' of parameter type '"
				+ param.getId() + "' spanning "
				+ param.getDimsToString() + " dimensions.");
	}
}