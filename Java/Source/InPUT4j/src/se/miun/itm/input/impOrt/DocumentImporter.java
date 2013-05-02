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
package se.miun.itm.input.impOrt;

import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;

/**
 * A delegate implementation of a document importer, which simply delegates the document.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 *
 */
public class DocumentImporter implements InPUTImporter<Document> {

	private Document document;

	public DocumentImporter(Document doc) {
		document = doc;
	}

	public DocumentImporter() {
	}

	@Override
	public String getInfo() {
		return "Simply returns documents for import purposes";
	}

	public synchronized void resetContext(Document document) {
		this.document = document;
	}

	@Override
	public synchronized Document impOrt() throws InPUTException {
		if (document == null)
			throw new InPUTException("You have not set the context for importer DocumentImporter appropriately. The document is not initialized.");
		return document;
	}
}