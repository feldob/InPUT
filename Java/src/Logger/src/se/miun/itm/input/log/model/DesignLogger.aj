package se.miun.itm.input.log.model;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.element.Value;

public aspect DesignLogger {

	private static Logger log = LoggerFactory.getLogger("design");
	
	pointcut designInit() : execution(public Design.new(..)) && if(InPUTConfig.isLoggingActive());
	
	pointcut export(InPUTExporter<?> exporter) : execution(* Design.export(InPUTExporter<?>)) && args(exporter) && if(InPUTConfig.isLoggingActive());

	pointcut setValue(Value<?> value) : (execution(private void Design.updateCacheForIndexedValue(Value<?>)) || execution(private void Design.updateElementCache(Value<?>))) && args(value) && if(InPUTConfig.isLoggingActive());

	before(Design design, Value<?> value) : setValue(value) && target(design){
		log.info(design.getId() + "." + value.getId() + "=" + value.valueToString());
	}

	after(Design design) returning() : target(design) && (designInit()) {
		log.info("Creating design '" + design.getId()
				+ "' for space '" + design.getSpace().getId()
				+ "'.");
	}
}