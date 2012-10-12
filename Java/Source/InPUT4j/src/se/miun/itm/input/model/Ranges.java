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

import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.util.Q;

/**
 * Each numeric parameter (implicitly) defines a range of valid values. This
 * range information is all available via its instance of the ranges class.
 * 
 * @author Felix Dobslaw
 * 
 * TODO for performance reasons, make ranges less generic (no casting!)!
 */
public class Ranges {

	private boolean minIncl = false;

	private boolean maxIncl = false;

	private String minExpr = null;

	private String maxExpr = null;

	private Comparable<?> max = null;

	private Comparable<?> min = null;

	private final Numeric type;

	private final NParam param;

	public Ranges(NParam param) throws InPUTException {
		this.param = param;
		this.type = param.getNumericType();

		setStaticMin(true, param.getAttributeValue(Q.INCL_MIN));
		setStaticMin(false, param.getAttributeValue(Q.EXCL_MIN));
		if (minExpr == null)
			min = type.getMin();

		setStaticMax(true, param.getAttributeValue(Q.INCL_MAX));
		setStaticMax(false, param.getAttributeValue(Q.EXCL_MAX));
		if (maxExpr == null)
			max = type.getMax();
	}

	private void setStaticMin(boolean inclusive, String expr)
			throws InPUTException {

		if (expr != null) {
			if (minExpr == null || (minExpr != null && minIncl == inclusive)) {

				minIncl = inclusive;
				minExpr = expr;

				if (!param.isMinDependent()) {
					min = initMin(expr);
					if (max != null && compareTo(min, max) > 0) {
						throw new InPUTException(
								param.getId()
										+ ": A minimum value has to be smaller than the maximum value.");
					}
				}
			} else
				throw new InPUTException(
						param.getId()
								+ ": exclMax and inclMax attributes are mutual exclusive.");
		}
	}

	private int compareTo(Comparable<?> min, Comparable<?> max) {
		int result = 0;
		switch (type) {
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

	private Comparable<?> initMin(String expression) {
		Comparable<?> strongMin;
		if (expression == null)
			strongMin = type.getMin();
		else {
			Comparable<?> entry = type.parse(expression);
			if (minIncl)
				strongMin = entry;
			else
				strongMin = add(type, entry, type.getAtom());
		}
		return strongMin;
	}

	public void setDynamicMin(String expression) {
		min = initMin(expression);
	}

	public void setDynamicMax(String expression) {
		max = initMax(expression);
	}

	private void setStaticMax(boolean inclusive, String expr)
			throws InPUTException {
		if (expr != null) {
			if (maxExpr == null || (maxExpr != null && maxIncl == inclusive)) {

				maxIncl = inclusive;
				maxExpr = expr;

				if (!param.isMaxDependent()) {
					max = initMax(expr);
					if (min != null && compareTo(min, max) > 0)
						throw new InPUTException(
								param.getId()
										+ ": A maximum value has to be larger than the minimum value.");
				}
			} else
				throw new InPUTException(
						param.getId()
								+ ": exclMax and inclMax attributes are mutual exclusive.");
		}
	}

	private Comparable<?> initMax(String expression) {
		Comparable<?> strongMax;

		if (expression == null)
			strongMax = type.getMax();
		else {
			Comparable<?> entry = type.parse(expression);

			if (maxIncl)
				strongMax = entry;
			else
				strongMax = subtract(type, entry, type.getAtom());
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
					b.append("[");
				} else {
					b.append("]");
				}
				b.append(minExpr);
			}

			b.append(", ");

			if (maxExpr == null) {
				b.append("\\infty[");
			} else {
				b.append(maxExpr);
				if (maxIncl) {
					b.append("]");
				} else {
					b.append("[");
				}
			}
		} else {
			b.append("\\{true,false\\}");
		}

		return b.toString();
	}

	public Comparable<?> getStrongTypedMin() {
		return min;
	}

	public Comparable<?> getStrongTypedMax() {
		return max;
	}

	private static Comparable<?> add(Numeric type, Comparable<?> summand1,
			Comparable<?> summand2) {
		Comparable<?> result = null;
		switch (type) {
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

	private static Comparable<?> subtract(Numeric type, Comparable<?> summand1,
			Comparable<?> summand2) {
		Comparable<?> result = null;
		switch (type) {
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

	public NParam getParam() {
		return param;
	}
	
	public void checkValidity(Comparable<Comparable<?>> value) {
		switch (type) {
		case BOOLEAN:
			break;
		default:
			if (!param.isMinDependent() && minExpr != null && ((minIncl && value.compareTo(min) < 0) || (!minIncl && value.compareTo(min) <= 0)))
				throw new IllegalArgumentException(param.getDesignSpaceId() + ": The entered value \"" + value + "\" for the parameter with id \""+ param.getId()+"\" is out of range (below minimum threshold \"" + min + "\")." );
			else if (!param.isMaxDependent() && maxExpr != null && ((maxIncl && value.compareTo(max) > 0) || (!maxIncl && value.compareTo(max) >= 0)))
			throw new IllegalArgumentException(param.getDesignSpaceId() + ": The entered value \"" + value + "\" for the parameter with id \""+ param.getId()+"\" is out of range (above maximum threshold \"" + max + "\")." );
			break;
		}
	}
}