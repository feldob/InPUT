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
package se.miun.itm.input.model;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.eval.InputEvaluator;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.util.Q;

/**
 * Each numeric parameter (implicitly) defines a range of valid values. This
 * range information is all available via its instance of the ranges class.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 * 
 *                TODO for performance reasons, make ranges less generic (no
 *                casting!)!
 */
public class Ranges {

	private boolean minIncl = false;

	private boolean maxIncl = false;

	private String minExpr = null;

	private String maxExpr = null;

	private Comparable<?>[] max = null;

	private Comparable<?>[] min = null;

	private final Numeric type;

	private final String paramId;

	private final String spaceId;

	private final Set<Param<?>> maxDependencies;

	private final Set<Param<?>> minDependencies;

	private Object evalLock = Integer.MIN_VALUE;

	private static InputEvaluator eval;

	private Ranges(String spaceId, String paramId, Numeric type,
			Set<Param<?>> minDependencies, Set<Param<?>> maxDependencies)
			throws InPUTException {
		this.type = type;
		this.spaceId = spaceId;
		this.paramId = paramId;
		this.minDependencies = minDependencies;
		this.maxDependencies = maxDependencies;
	}

	private InputEvaluator getEval() throws InPUTException {
		if (eval == null)
			synchronized (evalLock) {
				eval = InPUTConfig.getValue(Q.EVALUATOR);
			}
		return eval;
	}

	public Ranges(Ranges ranges) throws InPUTException {
		this(ranges.spaceId, ranges.paramId, ranges.type,
				ranges.minDependencies, ranges.maxDependencies);
		this.minIncl = ranges.minIncl;
		this.maxIncl = ranges.maxIncl;
		this.minExpr = ranges.minExpr;
		this.maxExpr = ranges.maxExpr;
		this.min = ranges.min;
		this.max = ranges.max;
	}

	public Ranges(String spaceId, String paramId, Numeric type,
			Set<Param<?>> minDependencies, String inclMin, String exclMin,
			Set<Param<?>> maxDependencies, String inclMax, String exclMax)
			throws InPUTException {
		this(spaceId, paramId, type, minDependencies, maxDependencies);

		initMin(type, inclMin, exclMin);
		initMax(type, inclMax, exclMax);
	}

	public Ranges(NParam param) throws InPUTException {
		this(param.getSpaceId(), param.getId(), Numeric.valueOf(param), param
				.getMinDependencies(), param.getAttributeValue(Q.INCL_MIN),
				param.getAttributeValue(Q.EXCL_MIN),
				param.getMaxDependencies(),
				param.getAttributeValue(Q.INCL_MAX), param
						.getAttributeValue(Q.EXCL_MAX));
	}

	private void initMax(Numeric type, String inclMax, String exclMax)
			throws InPUTException {
		setStaticMax(true, inclMax);
		setStaticMax(false, exclMax);
		if (maxExpr == null)
			max = new Comparable<?>[] { type.getMax() };
	}

	private void initMin(Numeric type, String inclMin, String exclMin)
			throws InPUTException {
		setStaticMin(true, inclMin);
		setStaticMin(false, exclMin);
		if (minExpr == null)
			min = new Comparable<?>[] { type.getMin() };
	}

	private void setStaticMin(boolean inclusive, String expr)
			throws InPUTException {

		if (expr != null) {
			if (minExpr == null || (minExpr != null && minIncl == inclusive)) {

				minIncl = inclusive;
				minExpr = expr;

				if (!isMinDependent()) {
					min = initMin(expr);

					for (int i = 0; i < min.length; i++) {
						if (max != null && compareTo(min[i], max[i]) > 0) {
							throw new InPUTException(
									paramId
											+ ": Each minimum value has to be smaller than the maximum value.");
						}

					}
				}
			} else
				throw new InPUTException(
						paramId
								+ ": exclMax and inclMax attributes are mutual exclusive.");
		}
	}

	@SuppressWarnings("incomplete-switch")
	private int compareTo(Comparable<?> min, Comparable<?> max) {
		int result = 0;
		switch (type) {
		case DECIMAL:
			result = ((BigDecimal) min).compareTo((BigDecimal) max);
			break;
		case DOUBLE:
			result = Double.compare((Double) min, (Double) max);
			break;
		case FLOAT:
			result = Float.compare((Float) min, (Float) max);
			break;
		case INTEGER:
			result = ((Integer) min).compareTo((Integer) max);
			break;
		case SHORT:
			result = ((Short) min).compareTo((Short) max);
			break;
		case LONG:
			result = ((Long) min).compareTo((Long) max);
			break;
		}
		return result;
	}

	private Comparable<?>[] initMin(String expression) {
		Comparable<?>[] strongMin;
		if (expression == null)
			strongMin = new Comparable<?>[] { type.getMin() };
		else {
			Comparable<?>[] entry = type.parse(expression);
			if (minIncl)
				strongMin = entry;
			else
				strongMin = new Comparable<?>[] { add(type, entry[0],
						type.getAtom()) };
		}
		return strongMin;
	}

	public void setDynamicMin(String expression) {
		minExpr = expression;
		min = initMin(expression);
	}

	public void setDynamicMax(String expression) {
		maxExpr = expression;
		max = initMax(expression);
	}

	private void setStaticMax(boolean inclusive, String expr)
			throws InPUTException {
		if (expr != null) {
			if (maxExpr == null || (maxExpr != null && maxIncl == inclusive)) {

				maxIncl = inclusive;
				maxExpr = expr;

				if (!isMaxDependent()) {
					max = initMax(expr);
					for (int i = 0; i < max.length; i++) {
						if (min != null && compareTo(min[i], max[i]) > 0)
							throw new InPUTException(
									paramId
											+ ": Each maximum value has to be larger than the minimum value.");
					}
				}
			} else
				throw new InPUTException(
						paramId
								+ ": exclMax and inclMax attributes are mutual exclusive.");
		}
	}

	private Comparable<?>[] initMax(String expression) {
		Comparable<?>[] strongMax;

		if (expression == null)
			strongMax = new Comparable<?>[] { type.getMax() };
		else {
			Comparable<?>[] entry = type.parse(expression);

			if (maxIncl)
				strongMax = entry;
			else
				strongMax = new Comparable<?>[] { subtract(type, entry[0],
						type.getAtom()) };
		}
		return strongMax;
	}

	public String getMinExpression() {
		return minExpr;
	}

	public String getMaxExpression() {
		return maxExpr;
	}

	public boolean includesMaximum() {
		return maxIncl;
	}

	public boolean includesMinimum() {
		return minIncl;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		if (type.compareTo(Numeric.BOOLEAN) != 0) {

			if (minExpr == null) {
				b.append("]-\\infty ");
			} else {
				if (minIncl) {
					b.append('[');
				} else {
					b.append(']');
				}
				b.append(minExpr);
			}

			appendComma(b);

			if (maxExpr == null) {
				b.append("\\infty[");
			} else {
				b.append(maxExpr);
				if (maxIncl) {
					b.append(']');
				} else {
					b.append('[');
				}
			}
		} else {
			b.append("\\{true,false\\}");
		}

		return b.toString();
	}

	private void appendComma(StringBuilder b) {
		String comma = null;
		switch (type) {
		case LONG:
		case SHORT:
		case INTEGER:
			comma = ", \\dots, ";
			break;

		default:
			comma = ", ";
			break;
		}
		b.append(comma);
	}

	public Comparable<?>[] getStrongTypedMin() {
		return min;
	}

	public Comparable<?>[] getStrongTypedMax() {
		return max;
	}

	@SuppressWarnings("incomplete-switch")
	private static Comparable<?> add(Numeric type, Comparable<?> summand1,
			Comparable<?> summand2) {
		Comparable<?> result = null;
		switch (type) {
		case DECIMAL:
			result = ((BigDecimal) summand1).add((BigDecimal) summand2);
			break;
		case DOUBLE:
			result = (Double) summand1 + (Double) summand2;
			break;
		case FLOAT:
			result = (Float) summand1 + (Float) summand2;
			break;
		case INTEGER:
			result = (Integer) summand1 + (Integer) summand2;
			break;
		case SHORT:
			result = (Short) summand1 + (Short) summand2;
			break;
		case LONG:
			result = (Long) summand1 + (Long) summand2;
			break;
		}
		return result;
	}

	@SuppressWarnings("incomplete-switch")
	private static Comparable<?> subtract(Numeric type, Comparable<?> summand1,
			Comparable<?> summand2) {
		Comparable<?> result = null;
		switch (type) {
		case DECIMAL:
			result = ((BigDecimal) summand1).subtract((BigDecimal) summand2);
			break;
		case DOUBLE:
			result = (Double) summand1 - (Double) summand2;
			break;
		case FLOAT:
			result = (Float) summand1 - (Float) summand2;
			break;
		case INTEGER:
			result = (Integer) summand1 - (Integer) summand2;
			break;
		case SHORT:
			result = (Short) summand1 - (Short) summand2;
			break;
		case LONG:
			result = (Long) summand1 - (Long) summand2;
			break;
		}
		return result;
	}

	public void checkValidity(String paramId, Object value, ElementCache elementCache)
			throws InPUTException {
		if (isOfValidPlainType(value)) {
			checkValidPlainValidity(paramId, value, elementCache);
		} else if (value.getClass().isArray()) {
			checkValidArrayType(paramId, value, elementCache);
		} else
			throw new InPUTException("The value " + value.toString()
					+ " is not of correct primitive type, it is of type \""
					+ value.getClass().getName() + "\". Type \""
					+ type.getNumClass().getName() + "\" or \""
					+ type.getPrimitiveClass().getName() + "\" were expected.");

	}

	@SuppressWarnings("unchecked")
	private void checkValidPlainValidity(String paramId, Object value, ElementCache elementCache)
			throws InPUTException {
		Comparable<Comparable<?>> theValue = (Comparable<Comparable<?>>) value;
		
		Value<?> current = elementCache.get(paramId);
		
		if (current!= null && current.isArrayType()) {
			throw new InPUTException("You entered the plain value '"+ value +"' for '" + paramId + "', but an array was expected.");
		}
		
		switch (type) {
		case BOOLEAN:
			break;
		default:
			Ranges evaluatedRanges = initEvaluatedRanges(elementCache);

			checkMinima(theValue, evaluatedRanges);
			checkMaxima(theValue, evaluatedRanges);

		}
	}

	private void checkMaxima(Comparable<Comparable<?>> theValue,
			Ranges evaluatedRanges) {

		Comparable<?>[] extremas = evaluatedRanges.getStrongTypedMax();

		if (extremas.length == 0)
			return;
		
		boolean violates = true;
		for (Comparable<?> extrema : extremas) {
			if (theValue.compareTo(extrema) <= 0) {
				violates = false;
				break;
			}
		}

		if (violates)
			throw new IllegalArgumentException(spaceId
					+ ": The entered value \"" + theValue
					+ "\" for the parameter with id \"" + paramId
					+ "\" is out of range (above maximum threshold \"" + max[0]
					+ "\").");
	}

	private void checkMinima(Comparable<Comparable<?>> theValue,
			Ranges evaluatedRanges) {

		Comparable<?>[] extremas = evaluatedRanges.getStrongTypedMin();

		if (extremas.length == 0)
			return;

		boolean violates = true;
		for (Comparable<?> extrema : extremas) {
			if (theValue.compareTo(extrema) >= 0) {
				violates = false;
				break;
			}
		}

		if (violates)
			throw new IllegalArgumentException(spaceId
					+ ": The entered value \"" + theValue
					+ "\" for the parameter with id \"" + paramId
					+ "\" is out of range (below minimum threshold \"" + min[0]
					+ "\").");
	}

	private void checkValidArrayType(String paramId, Object value, ElementCache elementCache) throws InPUTException {
		Value<?> current = elementCache.get(paramId);
		Object flag = value;
		if (current != null && current.getValue() != null) {
			Object currentValue = current.getInputValue(null);
			//TODO must be turned around to flag too!
			while(currentValue.getClass().isArray())
			{
				currentValue = Array.get(currentValue, 0);
				if (!flag.getClass().isArray())
					throw new InPUTException("The dimension for the set value in " + paramId + " is wrong.");
				flag = Array.get(flag, 0);
			}
			
			if (flag.getClass().isArray())
				throw new InPUTException("The dimension for the set value in " + paramId + " is wrong.");
		}
		
//		Class<?> compType = value.getClass().getComponentType();
//		if (compType.isAssignableFrom(type.getPrimitiveClass())
//				|| compType.isAssignableFrom(type.getNumClass()))
//			return;
//		throw new InPUTException("The datatype for the set value for parameter '" + paramId + "' is wrong. '"+ type.getPrimitiveClass().getName() + "' expected, but was '" + compType  + "'.");
	}

	private boolean isOfValidPlainType(Object value) {
		return type.getPrimitiveClass().isInstance(value)
				|| type.getNumClass().isInstance(value);
	}

	private Ranges initEvaluatedRanges(ElementCache elementCache)
			throws InPUTException {
		Ranges evaluatedRanges;
		if (!isIndependant()) {

			Map<String, Object> vars = initVarsFromElementCache(elementCache);
			evaluatedRanges = getEval().evaluate(this, vars);
		} else
			evaluatedRanges = this;
		return evaluatedRanges;
	}

	private Map<String, Object> initVarsFromElementCache(
			ElementCache elementCache) {
		Map<String, Object> dependencies = new HashMap<String, Object>();
		addDependencies(elementCache, dependencies, getMinDependencies());
		addDependencies(elementCache, dependencies, getMaxDependencies());
		return dependencies;
	}

	private void addDependencies(ElementCache elementCache,
			Map<String, Object> dependencies, Set<Param<?>> params) {
		for (Param<?> param : params)
			dependencies.put(param.getId(), elementCache.get(param.getId())
					.getInputValue());
	}

	public boolean isIndependant() {
		return !(isMaxDependent() || isMinDependent());
	}

	public boolean isMinDependent() {
		return !minDependencies.isEmpty();
	}

	public Set<Param<?>> getMinDependencies() {
		return minDependencies;
	}

	public boolean isMaxDependent() {
		return !maxDependencies.isEmpty();
	}

	public Set<Param<?>> getMaxDependencies() {
		return maxDependencies;
	}

	public Object parse(String valueString) {
		return type.parseSingle(valueString);
	}

	public Object next(Random rng, Map<String, Object> vars)
			throws InPUTException {
		Ranges ranges = getEval().evaluate(this, vars);
		return type.random(ranges, rng);
	}

	@SuppressWarnings("incomplete-switch")
	public String ensureType(String expression) {
		switch (type) {
		case INTEGER:
			expression = "" + new BigDecimal(expression).intValue();
			break;
		case SHORT:
			expression = "" + new BigDecimal(expression).shortValue();
			break;
		case LONG:
			expression = "" + new BigDecimal(expression).longValue();
			break;
		}
		return expression;
	}

	public Numeric getType() {
		return type;
	}

	public boolean isCountable() {
		return type.isCountable();
	}

	public void checkTypeSafety(Object value) throws InPUTException {
		if ((!type.getPrimitiveClass().isInstance(value))
				&& !type.getNumClass().isInstance(value)) {
			if (value.getClass().isArray()) {
				Class<?> componentType = getComponentType(value);
				if (isNotCorrectNumericType(value, componentType))
					throw new InPUTException("The value \"" + value.toString()
							+ "\" for parameter '" + paramId +"' is of the wrong type. \""
							+ type.getPrimitiveClass().getName() + "\" or \""
							+ type.getNumClass()
							+ "\" was expected, but was \""
							+ value.getClass().getName() + "\".");
			}
		}
	}

	public boolean isNotCorrectNumericType(Object value, Class<?> componentType) {
		return !type.equals(Numeric.DECIMAL)
				&& (!componentType.equals(type.getPrimitiveClass())
						&&
						(!componentType.equals(type.getNumClass())) && !type
						.getNumClass().isInstance(value));
	}

	private Class<?> getComponentType(Object value) {
		Class<?> type = null;
		while (value.getClass().isArray()){
			type = value.getClass().getComponentType();
			value = Array.get(value, 0);
		}
		return type;
	}
}