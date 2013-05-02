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
package se.miun.itm.input.aspects;

import java.io.IOException;

import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.InPUTException;

/**
 * Allows for the import and return of an object customizable of a user defined
 * type, using inversion of control.
 * 
 * @author Felix Dobslaw
 * 
 * @param <T>
 * @param <G>
 */
public interface Importable<T, G> {

	/**
	 * imports an object of a user defined type.
	 * 
	 * @param importer
	 * @return
	 * @throws InPUTException
	 * @throws IOException 
	 */
	T impOrt(InPUTImporter<G> importer) throws InPUTException, IOException;
}