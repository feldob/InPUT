package se.miun.itm.input.log.impOrt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.Document;

public aspect XMLImportLogger {

	private static Logger log = LoggerFactory.getLogger("import");

	pointcut impOrt() : args()
	&& (execution(Document XMLFileImporter.impOrt())) && if(InPUTConfig.isLoggingActive());

	before(XMLFileImporter importer) : target(importer) && impOrt() {
			log.info("Importing design file '" + importer.getInfo()
			+ " ...");
	}
	
	after() returning() : impOrt() {
			log.info("... successful.");
	}
}