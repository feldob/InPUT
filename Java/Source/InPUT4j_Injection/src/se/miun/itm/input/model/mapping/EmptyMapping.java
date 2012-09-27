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

import java.lang.reflect.Constructor;
import java.util.List;

import se.miun.itm.input.model.Numeric;
import se.miun.itm.input.model.param.Param;

/**
 * A dummy mapping that becomes integrated for each parameter without an explicit user defined mapping element. 
 * @author Felix Dobslaw
 *
 */
public class EmptyMapping extends AMapping {

	public EmptyMapping(Param param) {
		super(param.getId(), null, null);
	}

	@Override
	public String getComponentId() {
		return null;
	}

	@Override
	public String getConstructorSignature() {
		return null;
	}

	@Override
	public Class<?> getWrapperClass() {
		return null;
	}

	@Override
	public String getWrapperGetter() {
		return null;
	}

	@Override
	public String getWrapperSetter() {
		return null;
	}

	@Override
	public boolean hasWrapper() {
		return false;
	}

	@Override
	public Constructor<?> getWrapperConstructor(Numeric numericType) {
		return null;
	}

	@Override
	public IMappings getCodeMappings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDependee(IMapping dependee) {
	}

	@Override
	public boolean isIndependent() {
		return true;
	}

	@Override
	public boolean dependsOn(IMapping dependant) {
		return false;
	}

	@Override
	public boolean containsInConstructorSignature(IMapping mapping) {
		return false;
	}
	@Override
	public int compareTo(IMapping o) {
		return getId().compareTo(o.getId());
	}

	@Override
	public List<IMapping> getDependees() {
		return null;
	}

	@Override
	public Wrapper getWrapper() {
		return null;
	}

	@Override
	public boolean isComplex() {
		return false;
	}

	@Override
	public Complex getComplex() {
		return null;
	}
}