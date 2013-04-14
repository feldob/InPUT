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
package se.miun.itm.input.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.FileNameAssigner;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;

/**
 * Exports the design in it's current state to an XML file.
 * 
 * @param outputPathName
 *            The full path of the file for algorithm design export
 * @NotThreadSafe (name and exporter are related!)
 * 
 * @throws InputException
 */
public class XMLArchiveExporter extends FileNameAssigner implements InPUTExporter<Void> {

	private final XMLOutputter outputter = new XMLOutputter(
			Format.getPrettyFormat());

	public XMLArchiveExporter() {
		this("");
	}
	
	public XMLArchiveExporter(String fileName) {
		super(fileName);
	}

	@Override
	public synchronized void resetFileName(String fileName) {
		if (!fileName.contains(".xml"))
			fileName += ".xml";
		super.resetFileName(fileName);
	}
	
	@Override
	public synchronized Void export(Document xml) throws InPUTException{
			try {
				outputter.output(xml, new BufferedWriter(new FileWriter(fileName)));
			} catch (IOException e) {
				throw new InPUTException("The xml file could not be written to file " +fileName + " due to an IO error.", e);
			}
		return null;
	}

	@Override
	public Void export(Exportable input) throws InPUTException{
		throw new InPUTException(
				"The export of complex InPUT structures to plain XML is not supported. Look for the ZipExporter instead."); 
	}
}
