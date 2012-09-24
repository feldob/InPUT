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
package se.miun.itm.input.eval;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Ranges;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;

/**
 * The InPUT evaluation engine, in which evaluators are hooked into.
 * @author Felix Dobslaw
 */
public class EvaluationEngine {

	/**
	 * for a given evaluation engine, and a set of ranges, process the logical or numerical expressions, and return 
	 * the evaluated ranges.
	 * @param eval
	 * @param ranges
	 * @param vars
	 * @return
	 * @throws InPUTException
	 */
	public static Ranges evaluate(InputEvaluator eval, Ranges ranges, Map<String, Object> vars) throws InPUTException{
		Ranges newRanges;
		NParam param = ranges.getParam();
		if (param.isIndependant()) {
			newRanges = ranges;
		} else {
			newRanges = new Ranges(param);
			String expression;
			Set<String> usedVars = new HashSet<String>();
			String id;

			Object extremeValue;
			if (param.isMinDependent()) {
				for (Param dep : param.getMinDependencies()) {
					id = dep.getId();
					usedVars.add(id);
					eval.putVariable(id, vars.get(id));
				}

				extremeValue = ranges.getMinExpression();
				if (extremeValue != null) {
					expression = extremeValue.toString();
					expression = evaluate(eval, param, expression, usedVars);
					newRanges.setDynamicMin(expression);
				}
			}

			if (param.isMaxDependent()) {
				for (Param dep : param.getMaxDependencies()) {
					id = dep.getId();
					usedVars.add(id);
					eval.putVariable(id, vars.get(id));
				}

				extremeValue = ranges.getMaxExpression();
				if (extremeValue != null) {
					expression = extremeValue.toString();
					expression = evaluate(eval, param, expression, usedVars);
					newRanges.setDynamicMax(expression);
				}

			}
			eval.clearVariables();
		}
		return newRanges;

	}

	private static String evaluate(InputEvaluator eval, NParam param,
			String expression, Set<String> vars) throws InPUTException {
		String value = null;
		if (expression != null)
		{
			try {
				value = eval.evaluate(param, vars, expression);
			} catch (Exception e) {
				throw new InPUTException(
						"The evaluation engine could not process the expression'"
								+ expression + "'.", e);
			}
		}
		return value;
	}
}