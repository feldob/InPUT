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
package se.miun.itm.input.model.mapping;

import java.lang.reflect.Method;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * A complex type is one that allows user defined data structures to be used
 * instead of arrays in collection type parameter definitions. That way,
 * arbitrary semantics can be applied when a collection of choices is added to
 * the collection.
 * 
 * @author Felix Dobslaw
 * 
 */
public class Complex {

	private final Class<?> type;

	private final Class<?> commonType;

	private final String addHandleName;

	private Method addHandle;

	public Complex(Element complex, Element mapping) throws InPUTException {
		type = initType(complex);
		addHandleName = complex.getAttributeValue(Q.ADD_ATTR);
		this.commonType = initCommonType(mapping);
	}

	private Class<?> initCommonType(Element mapping) throws InPUTException {
		try {
			return Class.forName(mapping.getAttributeValue(Q.TYPE_ATTR));
		} catch (ClassNotFoundException e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	private Class<?> initType(Element complex) throws InPUTException {
		try {
			return Class.forName(complex.getAttributeValue(Q.TYPE_ATTR));
		} catch (ClassNotFoundException e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	public void invokeAdd(Object complexObject, Object entry)
			throws InPUTException {
		if (addHandle == null)
			initHandle();
		try {
			addHandle.invoke(complexObject, entry);
		} catch (Exception e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	private void initHandle() throws InPUTException {
		try {
			addHandle = type.getMethod(addHandleName,
					new Class<?>[] { commonType });
		} catch (Exception e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	public Object newInstance() throws InPUTException {
		try {
			return type.newInstance();
		} catch (Exception e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}
}
