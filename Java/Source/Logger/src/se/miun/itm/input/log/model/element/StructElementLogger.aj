package se.miun.itm.input.log.model.element;

import org.jdom2.Content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.miun.itm.input.model.element.SValue;
import se.miun.itm.input.model.param.Param;

public aspect StructElementLogger {

	private static Logger log = LoggerFactory.getLogger("element");
	
	pointcut addContent(Content subValue) : args(subValue) && (execution(private void StructElement.warning(Content)));
	
	pointcut addSubValue(Object newValue, Param subParam) : args(newValue, subParam) && (execution(private void StructElement.addSubValue(Object, ParamElement)));
	
	after(SValue element, Object newValue, Param subParam) returning() : target(element) && addSubValue(newValue, subParam) {
	if (!subParam.hasGetHandle()) {
		log.warn(subParam.getId()
			+ ": The subParam of this type cannot be extracted rom the superParam, because the get handle is deactivated in the codemapping. This way, the value has to be set for '"
			+ subParam.getId()
			+ " after this call of set, in order to ensure consistency.");
		}
	}
	
	after(SValue element, Content subValue) : addContent(subValue) && this(element) {
	log.error(element.getId() + ": A value of type '"
			+ subValue.getValue()
			+ "' could not be added to an object of parameter '"
			+ element.getParam().getId() + "'.");
	}
}
