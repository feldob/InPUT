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

import se.miun.itm.input.aspects.Dependable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Numeric;
/**
 * A mapping represents a single InPUT mapping element within the code mapping tree.
 * It is an extended pojo of the content in the mapping file, with easy access to class definitions.
 * @author Felix Dobslaw
 *
 */
public interface IMapping extends Identifiable, Dependable<IMapping>, Comparable<IMapping>{
	
	String getComponentId();

	String getSetter();
	
	String getGetter();
	
	String getConstructorSignature();
	
	Class<?> getWrapperClass();
	
	String getWrapperGetter();

	String getWrapperSetter();

	boolean hasWrapper();

	Constructor<?> getWrapperConstructor(Numeric numericType) throws InPUTException;

	boolean hasGetHandle();

	boolean hasSetHandle();

	IMappings getCodeMappings();

	boolean containsInConstructorSignature(IMapping mapping);

	String getLocalId();

	Wrapper getWrapper();

	boolean isComplex();

	Complex getComplex();
}