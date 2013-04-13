package se.miun.itm.input.log.model;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.Document;

public aspect DesignSpaceLogger {

	private static Logger log = LoggerFactory.getLogger("space");

	pointcut nextDesign(String designId) : execution(public IDesign DesignSpace.nextDesign(String)) && args(designId) && if(InPUTConfig.isLoggingActive());

	pointcut nextEmptyDesign(String designId) : execution(public IDesign DesignSpace.nextEmptyDesign(String)) && args(designId) && if(InPUTConfig.isLoggingActive());
	
	pointcut finalConstructor() : ( execution(public DesignSpace.new(InputStream, InputStream)) || execution(DesignSpace.new(Document, String)) || execution(private DesignSpace.new(String, InputStream))) && if(InPUTConfig.isLoggingActive());

	after(DesignSpace space, String designId) : nextEmptyDesign(designId) && target(space) {
		log.info("Creating empty design '" + designId + "' for space '" + space.getId()
				+ "'.");
	}

	after(DesignSpace space, String designId) : nextDesign(designId) && target(space) {
		log.info("Creating design '" + designId + "' for space '" + space.getId()
				+ "'.");
	}
	
	after(DesignSpace space) : finalConstructor() && this(space){
		log.info("Importing design space '" + space.getId() + "' defining "+ space.getSupportedParamIds().size() + " parameters.");
	}
}