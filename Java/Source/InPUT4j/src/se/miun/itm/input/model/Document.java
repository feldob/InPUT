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
package se.miun.itm.input.model;

import org.jdom2.DocType;
import org.jdom2.Element;

/**
 * A wrapper class that wraps third party library classes, in order to not make
 * users directly depend on them, and due to the restrictions of third party
 * class exposure to service consumers.
 * 
 * @author felix
 * 
 */
public class Document extends org.jdom2.Document {

	private static final long serialVersionUID = -6568993434628964523L;

	public Document(Element rootElement, DocType docType, String baseURI) {
		super(rootElement, docType, baseURI);
	}

	public Document(Element root) {
		super(root);
	}
}