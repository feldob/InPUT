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

import java.util.List;

/**
 * Dependable allows classes to define a dependency relationship.
 * 
 * @author Felix Dobslaw
 * 
 * @param <T>	The type that creates the dependency structure
 */
public interface Dependable<T> {

	/**
	 * Makes this dependable dependant of dependee
	 * @param dependee
	 */
	void addDependee(T dependee);
	
	/**
	 * Queries if this dependable object has no dependencies.
	 * @return
	 */
	boolean isIndependent();
	
	/**
	 * 
	 * Queries if this dependable object depends on dependant
	 * 
	 * @param dependant
	 * @return
	 */
	boolean dependsOn(T dependant);
	
	/**
	 * returns the set of dependees for this object.
	 * @return
	 */
	List<T> getDependees();
}