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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;

/**
 * Exports the design in it's current state to an OutputStream.
 * 
 * @ThreadSafe
 * 
 * @throws InputException
 */

public class ByteArrayExporter implements InPUTExporter<ByteArrayOutputStream> {

	private final XMLOutputter outputter = new XMLOutputter(
			Format.getPrettyFormat());

	@Override
	public ByteArrayOutputStream export(Exportable input) throws InPUTException {
		throw new InPUTException("unsupported so far.");
	}

	@Override
	public synchronized ByteArrayOutputStream export(Document xml) throws InPUTException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			outputter.output(xml, out);
		} catch (IOException e) {
			throw new InPUTException(
					"The xml could not be exported succesfully. The output stream could not be closed.",
					e);
		}
		return out;
	}

	@Override
	public String getInfo() {
		return "ByteArrayOutputStream";
	}

}
