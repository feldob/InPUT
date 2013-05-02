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

import se.miun.itm.input.model.InPUTException;

public interface Gettable {

	/**
	 * Returns the current parameter value of this design for parameter
	 * <code>paramId</code>.
	 * 
	 * @param paramId
	 *            The id of the parameter of interest in InPUT notion.
	 * @return The current parameter value of type <code>paramId</code>
	 * @throws InPUTException 
	 */
	<T> T getValue(String paramId) throws InPUTException;
	
	/**
	 * Returns the current parameter value of this design for parameter
	 * <code>paramId</code>.
	 * 
	 * @param paramId
	 *            The id of the parameter of interest in InPUT notion.
	 * @param actualParams
	 *            The actual parameters necessary to instantiate the object.
	 *            Only required if not using standard constructors.
	 * @return The current parameter value of type <code>paramId</code>
	 * @throws InPUTException 
	 */
	<T> T getValue(String string, Object[] actualParams) throws InPUTException;

	/**
	 * Returns the current parameter value of this design for parameter
	 * <code>paramId</code> as an InPUT identifier.
	 * 
	 * @param paramId
	 *            The id of the parameter of interest in InPUT notion.
	 * @return The InPUT String variant of the current parameter value of type
	 *         <code>paramId</code>
	 * @throws InPUTException 
	 */
	String getValueToString(String paramId) throws InPUTException;
}
