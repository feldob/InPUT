package se.miun.itm.input.log;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.InPUTException;

public aspect ExceptionLogger {

	private static Logger log = LoggerFactory.getLogger("exception");
	
	pointcut publicCall(): call(public * *.*(..)) && if(InPUTConfig.isLoggingActive());

	pointcut publicConstructorCall() : call(public new(..))&& if(InPUTConfig.isLoggingActive());

	after() throwing (InPUTException e): publicCall() {
				log.error(e.getMessage(), e);
	}

	after() throwing (InPUTException e): publicConstructorCall() {
			log.error(e.getMessage(), e);
	}
}