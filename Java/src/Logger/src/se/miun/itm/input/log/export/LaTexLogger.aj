/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package se.miun.itm.input.log.export;

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