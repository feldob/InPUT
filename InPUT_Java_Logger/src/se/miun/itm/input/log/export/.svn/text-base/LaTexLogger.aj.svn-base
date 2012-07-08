package se.miun.itm.input.log.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.export.LaTeXFileExporter;

public aspect LaTexLogger {

	private static Logger log = LoggerFactory.getLogger("export");
	
	pointcut appendDesignTableStart(StringBuilder b, String id) : args(b, id)
	&& (execution(void appendDesignTableStart(StringBuilder, String))) && if(InPUTConfig.isLoggingActive());
	
	pointcut appendDesignSpaceTableStart(StringBuilder b, String id) : args(b, id)
	&& (execution(void LaTeXFileExporter.appendDesignSpaceTableStart(StringBuilder, String)))&& if(InPUTConfig.isLoggingActive());
	
	pointcut writeFile(StringBuilder b) : args(b)
	&& (execution(void LaTeXFileExporter.writeFile(StringBuilder))) && if(InPUTConfig.isLoggingActive());

	before(LaTeXFileExporter exporter) : target(exporter) && appendDesignTableStart(StringBuilder, String) {
			log.info("exporting design space to file '"
					+ exporter.getInfo() + "' as LaTeX table ...");
	}
	
	after() returning() : writeFile(StringBuilder) {
			log.info("... successful.");
	}
	
	before(LaTeXFileExporter exporter) : target(exporter) && appendDesignSpaceTableStart(StringBuilder, String) {
			log.info("exporting design space to file '"
					+ exporter.getInfo() + "' as LaTeX table ...");
	}
	
}