/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */
package se.miun.itm.input.util.xml;

import java.util.concurrent.ArrayBlockingQueue;

import org.jdom2.input.SAXBuilder;

/**
 * A queue that receives and processes requests, according to the producer-
 * consumer model. The amount of producers can be customized.
 * 
 * @author Felix Dobslaw
 * 
 */
public class SAXBuilderQueue extends ArrayBlockingQueue<SAXBuilder> {

	private static final long serialVersionUID = 1402873458890729948L;

	private static final String CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
	private static final String VALIDATION = "http://xml.org/sax/features/validation";
	private static final String VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";
	private static final String PARSER = "org.apache.xerces.parsers.SAXParser";

	/**
	 * A queue with SaxBuilders that supports the processing of building requests, either with or without XML verification support.
	 * @param capacity	amount of producers
	 * @param verify
	 * @throws InterruptedException
	 */
	public SAXBuilderQueue(int capacity, boolean verify)
			throws InterruptedException {
		super(capacity);
		initBuilders(capacity, verify);
	}

	private void initBuilders(int capacity, boolean verify)
			throws InterruptedException {
		for (int i = 0; i < capacity; i++)
			put(createBuilder(verify));
	}

	private static SAXBuilder createBuilder(boolean verify) {
		SAXBuilder builder = new SAXBuilder(PARSER, verify);
		builder.setFeature(VALIDATION_SCHEMA, verify);
		builder.setFeature(VALIDATION, verify);
		builder.setFeature(CHECKING, verify);
		return builder;
	}
}
