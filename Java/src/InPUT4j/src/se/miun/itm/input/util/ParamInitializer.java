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
 */package se.miun.itm.input.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.mapping.IMapping;
import se.miun.itm.input.model.mapping.IMappings;

/**
 * The ParamInitializer organizes the input parameters according to the logical
 * dependency order in which they have to be instantiated. This is not necessary
 * the definition order, as InPUT xml descriptors do not restrict the user from
 * entering wrongly ordered entries into the design space files. However, in
 * case of circular definitions, no guarantees are provided.
 * 
 * @author Felix Dobslaw
 * 
 * @ThreadSafe
 */
public class ParamInitializer {

	/**
	 * receive the ordered parameter list for a given parameter tree, under the
	 * consideration of the initialiaztion order.
	 * b
	 * @param spaceTree
	 * @param mappings
	 * @return
	 */
	public static LinkedList<Element> getOrderedInitParams(Document spaceTree,
			IMappings mappings) {
		LinkedList<Element> params = new LinkedList<Element>();
		List<Element> children = spaceTree.getRootElement().getChildren();
		for (Element param : children)
			addToList(params, param);

		if (mappings != null) {
			makeMappingConsistent(params, mappings);
		}

		return params;
	}

	private static int getRootDist(Element elem) {
		int counter = 0;
		while (!elem.isRootElement()) {
			elem = elem.getParentElement();
			counter++;
		}
		return counter;
	}

	/**
	 * in case of a mapping, the order problem is extended to the constructor
	 * references in the mapping files.
	 * 
	 * @param params
	 * @param mappings
	 */
	private static void makeMappingConsistent(LinkedList<Element> params,
			IMappings mappings) {
		// first identify the dependent groups!
		String paramId;
		IMapping mapping;
		List<IMapping> dependants = new ArrayList<IMapping>();
		Map<String, Element> lookup = new HashMap<String, Element>();
		Map<Element, String> lookupRev = new HashMap<Element, String>();
		Map<IMapping, Element> lookupE = new HashMap<IMapping, Element>();
		Map<Element, Integer> positions = new HashMap<Element, Integer>();

		Element param;
		for (int i = 0; i < params.size(); i++) {
			param = params.get(i);
			paramId = ParamUtil.deriveParamId(param);
			mapping = mappings.getMapping(paramId);
			lookup.put(paramId, param);
			lookupRev.put(param, paramId);
			if (mapping != null) {
				lookupE.put(mapping, param);
				if (!mapping.isIndependent())
					dependants.add(mapping);
			}
			positions.put(param, i);
		}

		params = sortParams(params);

		// define order within the dependants (most dependent last!)
		Collections.sort(dependants);

		// for each dependant, look who they depend on
		List<IMapping> dependees;
		for (IMapping dep : dependants) {
			dependees = dep.getDependees();
			paramId = dep.getId();
			param = lookup.get(paramId);
			LinkedList<IMapping> dependeesNew = new LinkedList<IMapping>(
					dependees);
			int hullPos = getHullPosition(dependees, lookupE, params);
			processHull(hullPos, param, paramId, positions.get(param),
					lookupRev, lookupE, dependeesNew, params);
		}
	}

	private static LinkedList<Element> sortParams(LinkedList<Element> params) {
		LinkedList<Element> newParams = new LinkedList<Element>();
		for (int i = 0; i < params.size(); i++)
			addInRightSpot(params.get(i), newParams);
		return newParams;
	}

	private static void addInRightSpot(Element element,
			LinkedList<Element> newParams) {
		int counter = 0;
		for (int i = 0; i < newParams.size(); i++) {
			if (getRootDist(element) < getRootDist(newParams.get(i)))
				break;
			counter++;
		}
		newParams.add(counter, element);
	}

	/**
	 * A hull is a set of parameters that logically belong together. Structural
	 * Parameters, and Structural Choices have to be initiated following a
	 * certain logical order, in which parents SParam are always instantiated
	 * before their choices, but any parameter and choice requires the
	 * referenced parameters to be instantiated first. This function returns the
	 * position of a searched hull.
	 * 
	 * @param dependees
	 * @param lookup
	 * @param params
	 * @return
	 */
	private static int getHullPosition(List<IMapping> dependees,
			Map<IMapping, Element> lookup, LinkedList<Element> params) {
		// now, the local hull has to be placed BEHIND the last occuring
		// dependee in params.
		Element param, dependeeParam;
		int i = 0;
		while (!dependees.isEmpty()) {
			param = params.get(i);
			for (int j = 0; j < dependees.size(); j++) {
				dependeeParam = lookup.get(dependees.get(j));
				if (param.equals(dependeeParam)) {
					dependees.remove(j);
					break;
				}
			}
			i++;
		}
		return i;
	}

	/**
	 * the processing of the hull involves the identification of all parameters
	 * belonging to it, and the move to an appropriate position in the parameter
	 * order.
	 * 
	 * @param hullPos
	 * @param param
	 * @param dependantId
	 * @param position
	 * @param lookup
	 * @param lookupE
	 * @param dependees
	 * @param params
	 */
	private static void processHull(int hullPos, Element param,
			String dependantId, int position, Map<Element, String> lookup,
			Map<IMapping, Element> lookupE, LinkedList<IMapping> dependees,
			LinkedList<Element> params) {

		Element dependee = params.get(hullPos - 1);
		String dependeeId = lookup.get(dependee);

		String commonFrom = ParamUtil.deriveLowestNonSharedId(dependeeId,
				dependantId);
		LinkedList<Element> hull = new LinkedList<Element>();

		Element neighbor;
		String neighborId;
		int counter = position;
		while (counter < params.size()) {
			neighbor = params.get(counter);
			neighborId = lookup.get(neighbor);
			// think of not destroying the dependees structure and add it AFTER
			// the most non-common entry!
			if (neighborId.startsWith(commonFrom))
				hull.addLast(neighbor);
			else
				break;
			counter++;
		}

		// add the children that it has before
		counter = position;
		while (counter > 0) {
			neighbor = params.get(counter - 1);
			neighborId = lookup.get(neighbor);
			if (neighborId.startsWith(commonFrom))
				hull.addFirst(neighbor);
			else
				break;
			counter--;
		}

		// now remove them from the params list.
		for (int i = 0; i < hull.size(); i++) {
			params.remove(counter);
		}

		// make sure that they are added at the right position, right before
		// commonStart!
		String commonTo = ParamUtil.deriveLowestNonSharedId(dependantId,
				dependeeId);

		// update the hull position (in case the entries where before the last
		// dependee!)
		hullPos = getHullPosition(dependees, lookupE, params);
		addHull(hullPos, hull, commonTo, lookup, params);
	}

	/**
	 * for a given hull, moves it to position hullPos.
	 * 
	 * @param hullPos
	 * @param hull
	 * @param commonFrom
	 * @param lookup
	 * @param params
	 */
	private static void addHull(int hullPos, LinkedList<Element> hull,
			String commonFrom, Map<Element, String> lookup,
			LinkedList<Element> params) {
		String entryPointId;
		int pos = hullPos - 1;
		do {
			pos++;
			entryPointId = lookup.get(params.get(pos));
		} while (entryPointId.startsWith(commonFrom));

		params.addAll(pos, hull);
	}

	/**
	 * adds a parameter to the parameter list, depending on its kind.
	 * 
	 * @param params
	 * @param param
	 */
	private static void addToList(List<Element> params, Element param) {
		// add a numeric straight away
		if (param.getName().equals(Q.NPARAM))
			params.add(param);
		else if (param.getName().equals(Q.SPARAM)) {
			addStructParamToList(params, param);
		} else if (param.getName().equals(Q.SCHOICE))
			addStructChoiceToList(params, param);
	}

	private static void addStructChoiceToList(List<Element> params,
			Element param) {
		List<Element> children = param.getChildren();
		for (Element child : children)
			addToList(params, child);
		params.add(param);
	}

	private static void addStructParamToList(List<Element> params, Element param) {
		List<Element> choices = new ArrayList<Element>();
		List<Element> children = param.getChildren();

		// first add all param children
		for (Element child : children)
			if (child.getName().equals(Q.SPARAM)
					|| child.getName().equals(Q.NPARAM))
				addToList(params, child);
			else
				choices.add(child);

		// then add yourself
		params.add(param);

		// eventually, add all choices
		for (Element choice : choices)
			addToList(params, choice);
	}
}