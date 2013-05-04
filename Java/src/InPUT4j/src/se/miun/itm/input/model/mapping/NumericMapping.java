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
import java.util.List;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.util.Q;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public class NumericMapping extends Mapping {

	private final Wrapper wrapper;
	
	public NumericMapping(Element mappingElement) throws InPUTException {
		super(mappingElement);
		wrapper = initWrapper(mappingElement);
	}

	/**
	 * For empty Mapping inheritance only.
	 * @param id
	 */
	protected NumericMapping(String id) {
		super(id, null, null);
		wrapper = null;
	}
	
	public NumericMapping(String id, NumericMapping mapping) {
		super(id, mapping.getSetter(), mapping.getGetter());
		wrapper = mapping.getWrapper();
	}
	
	@Override
	public IMapping clone(String id, IMapping mapping) {
		return new NumericMapping(id, this);
	}

	private Wrapper initWrapper(Element mapping) throws InPUTException {
		List<Element> children = mapping.getChildren();
		Element wrap;
		if (children.size() > 0) {
			wrap = children.get(0);
			if (wrap.getName().equals(Q.WRAPPER))
				return new Wrapper(localId, wrap, mapping);
		}

		return null;
	}

	public Class<?> getWrapperClass() {
		return wrapper.getWrapperClass();
	}

	public String getWrapperGetter() {
		return wrapper.getGetter();
	}

	public String getWrapperSetter() {
		return wrapper.getSetter();
	}

	public boolean hasWrapper() {
		return wrapper != null;
	}

	public Wrapper getWrapper() {
		return wrapper;
	}

	public Constructor<?> getWrapperConstructor(Numeric numType)
			throws InPUTException {
		return wrapper.getWrapperConstructor(numType);
	}

	protected String initConstrSignature(Element mapping) {
		if (wrapper != null)
			return wrapper.getConstructorSignature();
		return null;
	}

	@Override
	public IMapping clone(String id, Element mapping) {
		return new NumericMapping(id, this);
	}
}
