package se.miun.itm.input.log.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.InPUTExporter;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.util.Q;

public aspect DesignLogger {

	private static Logger log = LoggerFactory.getLogger("design");

	pointcut designCall() : execution(protected Design.new(..)) && if(InPUTConfig.isLoggingActive());

	pointcut designInit() : execution(public Design.new(..)) && if(InPUTConfig.isLoggingActive());
	
	@SuppressWarnings("rawtypes")
	pointcut export(InPUTExporter exporter) : execution(* IDesign.export(InPUTExporter)) && args(exporter) && if(InPUTConfig.isLoggingActive());

	pointcut setValue(String paramId, Object value) : execution(public void IDesign.setValue(String, Object)) && args(paramId, value) && if(InPUTConfig.isLoggingActive());

	@SuppressWarnings("rawtypes")
	pointcut setElement(String id, Value valueE) : execution(void Design.setElement(String, ValueElement)) && args(id, valueE) && if(InPUTConfig.isLoggingActive());

	@SuppressWarnings("rawtypes")
	before(Design design, String id, Value valueE) : setElement(id, valueE) && target(design) {
		log.info("Parameter '" + id + "' for design '" +design.getId() + "' has been updated to value '"+ valueE.getAttributeValue(Q.VALUE_ATTR) +"'.");
	}

	before(IDesign design, String paramId, Object value) : setValue(paramId , value) && target(design){
		log.info("Set input parameter for instance '" + design.getId()
				+ "' of design space '" + design.getSpace().getId() + "' to object '"
				+ value.toString() + "'.");
	}

	after(Design design) returning() : target(design) && (designCall() || designInit()) {
		log.info("An empty design of type '" + design.getSpace().getId()
				+ "' by name '" + design.getId()
				+ "' has successfully been created.");
	}

	@SuppressWarnings("rawtypes")
	before(IDesign design, InPUTExporter exporter) : target(design) && export(exporter) {
		log.info("Print instance file of type '" + design.getSpace().getId()
				+ "' for instance '" + design.getId() + "' to '"
				+ exporter.getInfo() + "'.");
	}
}