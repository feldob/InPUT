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
 */package se.miun.itm.input.eval;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Ranges;
import se.miun.itm.input.model.param.Param;


/**
 * ensures for the supported primitive types that they hold an appropriate
 * format. Some of the evaluation engines do not support this (e.g. they return
 * a 1.0 as an integer, which breaks the integer parsing).
 * 
 * @author Felix Dobslaw
 * 
 * @ThreadSafe
 */
public abstract class AbstractEvaluator implements InputEvaluator {
	
	/**
	 * for a given evaluation engine, and a set of ranges, process the logical
	 * or numerical expressions, and return the evaluated ranges.
	 * 
	 * @param eval
	 * @param ranges
	 * @param vars
	 * @return
	 * @throws InPUTException
	 */
	@Override
	public Ranges evaluate(Ranges ranges, Map<String, Object> vars)
			throws InPUTException {
		Ranges newRanges;
		if (ranges.isIndependant()) {
			newRanges = ranges;
		} else {
			newRanges = new Ranges(ranges);
			String expression;
			Set<String> usedVars = new HashSet<String>();
			String id;

			Object extremeValue;
			if (ranges.isMinDependent()) {
				for (Param<?> dep : ranges.getMinDependencies()) {
					id = dep.getId();
					usedVars.add(id);
					putVariable(id, vars.get(id));
				}

				extremeValue = ranges.getMinExpression();
				if (extremeValue != null) {
					expression = extremeValue.toString();
					expression = evaluate(newRanges, expression, usedVars);
					newRanges.setDynamicMin(expression);
				}
			}

			if (ranges.isMaxDependent()) {
				for (Param<?> dep : ranges.getMaxDependencies()) {
					id = dep.getId();
					usedVars.add(id);
					putVariable(id, vars.get(id));
				}

				extremeValue = ranges.getMaxExpression();
				if (extremeValue != null) {
					expression = extremeValue.toString();
					expression = evaluate(newRanges, expression, usedVars);
					newRanges.setDynamicMax(expression);
				}

			}
			clearVariables();
		}
		return newRanges;

	}

	private String evaluate(Ranges ranges,
			String expression, Set<String> vars) throws InPUTException {
		String value = null;
		if (expression != null) {
			try {
				value = evaluate(vars, expression);
				value = ranges.ensureType(value);
			} catch (Exception e) {
				throw new InPUTException(
						"The evaluation engine could not process the expression '"
								+ expression + "'.", e);
			}
		}
		return value;
	}
	
	protected abstract String evaluate(Set<String> vars, String expression) throws Exception;

}