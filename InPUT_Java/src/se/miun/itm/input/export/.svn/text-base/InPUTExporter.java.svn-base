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

import se.miun.itm.input.aspects.InPUTExportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;

/**
 * The InPUT exporter is an abstract interface that allows elements of input type to be exported to different kinds of 
 * output formats.
 * @author Felix Dobslaw
 *
 * @param <T>
 */
public interface InPUTExporter<T> {

	/**
	 * export a given xml document, expected to be of InPUT type (CodeMapping, Design, or Design Space).
	 * @param xml
	 * @return
	 * @throws InPUTException
	 */
	T export(Document xml) throws InPUTException;

	/**
	 * export a given InPUT instance (e.g. Design, Design Space, InPUT).
	 * @param input
	 * @return
	 * @throws InPUTException
	 */
	T export(InPUTExportable input) throws InPUTException;

	String getInfo();
}