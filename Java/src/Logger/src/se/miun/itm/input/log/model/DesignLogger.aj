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
 */package se.miun.itm.input.log.model;

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