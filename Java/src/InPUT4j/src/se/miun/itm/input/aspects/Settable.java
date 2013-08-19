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
 */package se.miun.itm.input.aspects;

import se.miun.itm.input.model.InPUTException;

public interface Settable {

	/**
	 * Resets the current parameter value of this design for the parameter
	 * identified by <code>paramId</code> as an InPUT String.
	 * 
	 * @param paramId
	 *            The id of the parameter of interest in InPUT notion.
	 * @param obj
	 *            A valid value of the appropriate type for the parameter
	 *            identified by <code>paramId</code>.
	 * @throws InPUTException if <code>paramId</code> is an invalid id
	 * 			(<code>null</code> or unknown/nonexistent).
	 * @throws InPUTException if <code>obj</code> is of a type that doesn't
	 * 			match what was specified for <code>paramId</code>. This means
	 * 			that <code>null</code> is always an invalid value.
	 * @throws InPUTException if called on a read-only Design.
	 */
	void setValue(String paramId, Object obj) throws InPUTException;
}
