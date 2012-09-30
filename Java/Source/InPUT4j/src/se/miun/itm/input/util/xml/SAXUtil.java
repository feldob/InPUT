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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * The util class that is used by InPUT for most of the parsing and processing of XML data.
 * @author Felix Dobslaw
 */
public class SAXUtil {

	private static final BlockingQueue<SAXBuilder> verifyingBuilders;
	private static final BlockingQueue<SAXBuilder> simpleBuilders;

	static {
		verifyingBuilders = initBuilderQueue(2, true);
		simpleBuilders = initBuilderQueue(2, false);
	}

	public static se.miun.itm.input.model.Document build(
			InputStream inputStream, boolean verifying) throws InPUTException {

		SAXBuilder builder = takeBuilder(verifying);
		Document document;
		try {
			document = builder.build(inputStream);
		} catch (JDOMException e) {
			throw new InPUTException(
					"An error occured parsing a stream with an XML builder.", e);
		} catch (IOException e) {
			throw new InPUTException(
					"The stream file could not be located.", e);
		} finally {
			releaseBuilder(builder, verifying);
		}

		return getWrapper(document);
	}

	private static BlockingQueue<SAXBuilder> initBuilderQueue(int capacity,
			boolean verify) {
		BlockingQueue<SAXBuilder> queue = null;
		try {
			queue = new SAXBuilderQueue(capacity, verify);
		} catch (InterruptedException e) {
			System.out.println("internal error");
			e.printStackTrace();
		}
		return queue;
	}

	private static void releaseBuilder(SAXBuilder builder, boolean verifying) {
		try {
			if (verifying)
				verifyingBuilders.put(builder);
			else
				simpleBuilders.put(builder);
		} catch (InterruptedException e) {
			// TODO remove for production
			e.printStackTrace();
		}
	}

	private static SAXBuilder takeBuilder(boolean verifying) {
		SAXBuilder builder = null;
		try {
			if (verifying)
				builder = verifyingBuilders.take();
			else
				builder = simpleBuilders.take();
		} catch (InterruptedException e) {
			// TODO remove for production
			e.printStackTrace();
		}
		return builder;
	}

	private static se.miun.itm.input.model.Document getWrapper(Document document) {
		DocType dt = document.getDocType();
		document.setDocType(null);
		Element root = document.getRootElement();
		// a new root is just added to change the context.
		document.setRootElement(new Element(Q.SEED));
		return new se.miun.itm.input.model.Document(root, dt,
				document.getBaseURI());
	}

	public static se.miun.itm.input.model.Document build(
			String designSpaceFileName, boolean verifying)
			throws InPUTException {

		if (designSpaceFileName == null) {
			throw new InPUTException("The design space reference (ref attribute in root of the design) has to be set appropriately.");
		}
		SAXBuilder builder = takeBuilder(verifying);
		Document document;
		try {
			document = builder.build(designSpaceFileName);
		} catch (JDOMException e) {
			throw new InPUTException(
					"An error occured parsing file '" + designSpaceFileName
					+ "' with an XML builder.", e);
		} catch (IOException e) {
			throw new InPUTException(
					"The XML file could not be read from file '"
							+ designSpaceFileName + "'.", e);
		} finally {
			releaseBuilder(builder, verifying);
		}

		return getWrapper(document);
	}
}