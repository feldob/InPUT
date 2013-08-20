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
 */package se.miun.itm.input.model.mapping;

import java.lang.reflect.Constructor;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.util.Q;

/**
 * A wrapper is a class that allows numerical values to be wrapped for range
 * definitions in the code mappings file. This is of importance because many
 * third party frameworks have their own customized definitions of certain
 * ranges, a prominent example is the Probability class of the apache maths uncommons.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class Wrapper {

	private final Class<?> cLass;
	private final String getter;
	private Constructor<?> constructor;
	private final String setter;
	private final String constructorSignature;
	private final String paramId;

	public Wrapper(String localId, Element wrap, Element mapping)
			throws InPUTException {
		cLass = initWrapperClass(wrap);
		paramId = mapping.getAttributeValue(Q.ID_ATTR);
		getter = initGetter(mapping, wrap, localId);
		setter = initSetter(mapping, wrap, localId);
		constructorSignature = wrap.getAttributeValue(Q.CONSTR_ATTR);
	}

	private Constructor<?> initConstructor(Numeric numericType)
			throws InPUTException {
		Class<?>[] formalParam = { numericType.getPrimitiveClass() };
		try {
			constructor = cLass.getConstructor(formalParam);
		} catch (Exception e) {
			formalParam[0] = numericType.getNumClass();
			try {
				constructor = cLass.getConstructor(formalParam);
			} catch (NoSuchMethodException e1) {
				throw new InPUTException("There is no such constructor.", e1);
			} catch (SecurityException e1) {
				throw new InPUTException(
						"You do not have the right to call this constructor.",
						e1);
			}
		}
		return constructor;
	}

	private String initSetter(Element mapping, Element wrap, String localId) {
		if (wrap.getAttribute(Q.SET_ATTR) != null)
			return wrap.getAttributeValue(Q.SET_ATTR);
		return Q.SET_ATTR + localId;
	}

	private String initGetter(Element mapping, Element wrap, String localId) {
		if (wrap.getAttribute(Q.GET_ATTR) != null)
			return wrap.getAttributeValue(Q.GET_ATTR);
		return Q.GET_ATTR + localId;
	}

	public String getConstructorSignature() {
		return constructorSignature;
	}

	public String getGetter() {
		return getter;
	}

	public Class<?> getWrapperClass() {
		return cLass;
	}

	private Class<?> initWrapperClass(Element wrap) throws InPUTException {
		try {
			return Class.forName(wrap.getAttributeValue(Q.TYPE_ATTR));
		} catch (ClassNotFoundException e) {
			throw new InPUTException("There is no such class: "
					+ wrap.getAttributeValue(Q.TYPE_ATTR), e);
		}
	}

	public String getSetter() {
		return setter;
	}

	public Constructor<?> getWrapperConstructor(Numeric numType)
			throws InPUTException {
		if (constructor == null)
			initConstructor(numType);
		return constructor;
	}

	public void checkTypeSafety(Object value) throws InPUTException {
		if (!cLass.isInstance(value))
			throw new InPUTException("The value " + value.toString() + " for the wrapper class of parameter \""+paramId+"\" is not of the defined type \"" + cLass.getName() + "\".");
	}
}