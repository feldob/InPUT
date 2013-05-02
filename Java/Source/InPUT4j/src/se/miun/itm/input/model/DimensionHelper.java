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

import org.jdom2.Element;

import se.miun.itm.input.util.Q;

/**
 * A wrapper for dimensional information for a parameter; each parameter has a
 * dimensional default setup, defined by an instance of this type.
 * 
 * @author Felix Dobslaw
 * 
 *  @ThreadSafe
 */
public class DimensionHelper {

	public static final int[] DEFAULT_DIM = { 0 };

	public static int[] derive(final Element param) {
		int[] dimensions;
		String typeDefinition = param.getAttributeValue(Q.TYPE_ATTR);

		// 1) struct param without type definition, or
		// 2) numeric param without array in the type definition.
		if (((param.getName().equals(Q.SPARAM) || param.getName().equals(
				Q.SCHOICE)) && typeDefinition == null)
				|| typeDefinition.split(Q.ESCAPED_ARRAY_START).length == 1)
			// a simple single value element.
			dimensions = DEFAULT_DIM;
		else
			dimensions = deriveArray(typeDefinition);

		return dimensions;
	}

	private static int[] deriveArray(final String typeDefinition) {
		int[] dimensions;
		// get the type string representation
		// split it by array starts
		String[] paramTypeStrA = typeDefinition.split(Q.ESCAPED_ARRAY_START);
		// more than simple values, meaning a vector or matrix
		// two values imply one dimension, but multiple possible entries
		// (vector)
		dimensions = new int[paramTypeStrA.length - 1];
		for (int i = 1; i < paramTypeStrA.length; i++)
			dimensions[i - 1] = derive(paramTypeStrA[i]);
		return dimensions;
	}

	public static String toString(final int[] dimensions) {
		StringBuilder b = new StringBuilder();
		b.append(dimensions[0]);
		for (int j = 1; j < dimensions.length; j++) {
			b.append('x');
			b.append(dimensions[j]);
		}
		return b.toString();
	}

	private static Integer derive(final String value) {
		if (value.length() > 1) {
			String valueString = value.substring(0, value.length() - 1);
			return Integer.parseInt(valueString);
		} else
			return -1;
	}

}
