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

import java.util.Collection;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Identifiable;

/**
 * Each design space holding structural parameters, requires the definition of
 * implementation specific component mappings, so called code mappings. Code
 * mappings not only allow the setting as a key-value store, but can be extended
 * by constructor signature definitions, setter and getter identifiers, and even
 * more complex structures are supported, such as the collection abstraction
 * ("complex") or the use of wrappers for numerical values.
 * 
 * This mapping is solely a structural, and easy accessible pendant to the xml
 * file.
 * 
 * @author Felix Dobslaw
 * 
 */
public interface IMappings extends Identifiable, Exportable {

	/**
	 * receive the component, or class id, for the parameter id.
	 * 
	 * @param paramId
	 * @return
	 */
	String getComponentId(String paramId);

	/**
	 * receive the mapping element for a parameter id.
	 * 
	 * @param paramId
	 * @return
	 */
	IMapping getMapping(String paramId);

	/**
	 * returns all existing parameter ids that have a defined code mapping.
	 * 
	 * @return
	 */
	Collection<String> getMappingParameterIds();

	/**
	 * adds an alias to the code mappings, which is necessary for the treatment
	 * of mappings that refer to mapping types.
	 * 
	 * @param alias
	 * @param paramId
	 */
	public abstract void addAlias(String alias, String paramId);
}