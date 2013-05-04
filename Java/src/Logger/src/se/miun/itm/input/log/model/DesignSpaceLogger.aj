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