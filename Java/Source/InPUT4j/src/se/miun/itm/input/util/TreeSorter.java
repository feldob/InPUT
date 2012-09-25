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
package se.miun.itm.input.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Element;

import se.miun.itm.input.eval.ParamEvaluationOrderComparator;
import se.miun.itm.input.model.param.Param;

/**
 * Orders the parameter tree with respect to the numerical parameter min-max
 * dependencies.
 * 
 * @author Felix Dobslaw
 */
public class TreeSorter {

	/**
	 * take a design space tree and order it with respect to min max
	 * dependencies, so that parameters that require a context never get
	 * null-pointers.
	 * 
	 * @param parent
	 * @param comparator
	 */
	@SuppressWarnings("unchecked")
	public static void reorganizeTree(final Element parent,
			final ParamEvaluationOrderComparator<Element> comparator) {
		List<Param> params = (List<Param>)(List<?>)parent.getChildren();
		if (params != null && !params.isEmpty()) {
			List<Param> paramsCopy = new ArrayList<Param>();

			for (Param child : params) {
				reorganizeTree(child, comparator);
				paramsCopy.add(child);
				if (parent instanceof Param)
					addDependenciesToParent((Param) parent, child);
			}

			parent.removeContent();

			Collections.sort(paramsCopy, comparator);

			for (int i = 0; i < paramsCopy.size(); i++)
				parent.addContent(paramsCopy.get(i));
		}
	}

	/**
	 * instantiate the dependencies of children to their parents for an easier
	 * treatment of global order on each layer.
	 * 
	 * @param parent
	 * @param child
	 */
	private static void addDependenciesToParent(Param parent, Param child) {
		for (Param dependee : child.getDependees())
			if (!isUpperLevelRelevant(parent, dependee))
				parent.addDependee(dependee);
		for (Param minDependency : child.getMinDependencies())
			if (!isUpperLevelRelevant(parent, minDependency))
				parent.addMinDependency(minDependency);
		for (Param maxDependency : child.getMaxDependencies())
			if (!isUpperLevelRelevant(parent, maxDependency))
				parent.addMaxDependency(maxDependency);
	}

	/**
	 * Is the dependency more global than the given parameter context?
	 * 
	 * @param parent
	 * @param relationParam
	 * @return
	 */
	private static boolean isUpperLevelRelevant(Param parent,
			Param relationParam) {
		return relationParam.getId().startsWith(parent.getId());
	}
}
