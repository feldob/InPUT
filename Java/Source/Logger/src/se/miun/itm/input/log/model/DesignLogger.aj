package se.miun.itm.input.log.model;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.Exporter;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.element.Value;

public aspect DesignLogger {

	private static Logger log = LoggerFactory.getLogger("design");
	
	pointcut designImport() : execution(public Design.new(..)) && if(InPUTConfig.isLoggingActive());
	
	@SuppressWarnings("rawtypes")
	pointcut export(Exporter<?> exporter) : execution(* Design.export(Exporter<?>)) && args(exporter) && if(InPUTConfig.isLoggingActive());

	pointcut setValue(Value<?> value) : (execution(private void Design.updateCacheForIndexedValue(Value<?>)) || execution(private void Design.updateElementCache(Value<?>))) && args(value) && if(InPUTConfig.isLoggingActive());

	before(Design design, Value<?> value) : setValue(value) && target(design){
		log.info("Set parameter '" + value.getId() + "' of instance '" + design.getId()
				+ "' over design space '" + design.getSpace().getId() + "' to '"
				+ value.valueToString() + "'.");
	}

	after(Design design) returning() : target(design) && (designImport()) {
		log.info("A design for design space '" + design.getSpace().getId()
				+ "' with id '" + design.getId()
				+ "' is being imported.");
	}
}