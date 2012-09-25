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

import java.math.BigDecimal;
import java.util.Random;

/**
 * Numeric is the service enum that supplies lower and upper extrema for all
 * numeric values, and supports the random creation of numeric values of all supported kinds, given range restrictions.
 * 
 * @author Felix Dobslaw
 * 
 */
public enum Numeric {

	BOOLEAN(Boolean.class, Boolean.TYPE, "Boolean", Boolean.FALSE,
			Boolean.TRUE, true), INTEGER(Integer.class, Integer.TYPE, "Int",
			Integer.MIN_VALUE, Integer.MAX_VALUE, 1), LONG(Long.class,
			Long.TYPE, "Long", Long.MIN_VALUE, Long.MAX_VALUE, 1L), FLOAT(
			Float.class, Float.TYPE, "Float", Float.MIN_VALUE, 1f,
			Float.MIN_VALUE), DOUBLE(Double.class, Double.TYPE, "Double",
			Double.MIN_VALUE, 1d, Double.MIN_VALUE), DECIMAL(BigDecimal.class,
			BigDecimal.class, "Decimal", new BigDecimal(Long.MIN_VALUE),
			new BigDecimal(Long.MAX_VALUE), new BigDecimal(Double.MIN_VALUE)), SHORT(
			Short.class, Short.TYPE, "Short", Short.MIN_VALUE, Short.MAX_VALUE,
			1);

	private Class<?> numClass;
	private Class<?> numPrimClass;
	private String className;
	private Comparable<?> atom;
	private Comparable<?> min;
	private Comparable<?> max;

	public Class<?> getNumClass() {
		return numClass;
	}

	public Class<?> getPrimitiveClass() {
		return numPrimClass;
	}

	public String getClassId() {
		return className;
	}

	public Comparable<?> getAtom() {
		return atom;
	}

	public Comparable<?> getMin() {
		return min;
	}

	public Comparable<?> getMax() {
		return max;
	}

	private Numeric(Class<?> typeClass, Class<?> numPrimClass,
			String className, Comparable<?> min, Comparable<?> max,
			Comparable<?> atom) {
		this.numClass = typeClass;
		this.numPrimClass = numPrimClass;
		this.className = className;
		this.atom = atom;
		this.min = min;
		this.max = max;
	}

	public static Object randomValue(Numeric type, Ranges ranges, Random rng) {

		switch (type) {
		case DECIMAL:
			return randomdecimal(type, ranges, rng);
		case INTEGER:
			return randominteger((Integer) ranges.getStrongTypedMin(),
					(Integer) ranges.getStrongTypedMax(), rng);
		case LONG:
			return randomlong((Long) ranges.getStrongTypedMin(),
					(Long) ranges.getStrongTypedMax(), rng);
		case FLOAT:
			return randomfloat((Float) ranges.getStrongTypedMin(),
					(Float) ranges.getStrongTypedMax(), rng);
		case DOUBLE:
			return randomdouble((Double) ranges.getStrongTypedMin(),
					(Double) ranges.getStrongTypedMax(), rng);
		case BOOLEAN:
			return rng.nextBoolean();
		case SHORT:
			return randomshort((Short) ranges.getStrongTypedMin(),
					(Short) ranges.getStrongTypedMax(), rng);
		}
		return null;

	}

	private static BigDecimal randomdecimal(Numeric type, Ranges ranges,
			Random rng) {
		BigDecimal min, max;

		if (ranges.getMinExpression() == null)
			min = (BigDecimal) type.getMin();
		else {
			BigDecimal entry = (BigDecimal) ranges.getStrongTypedMin();
			if (ranges.includesMinimum())
				min = entry;
			else
				min = entry.add((BigDecimal) type.getAtom());
		}

		if (ranges.getMaxExpression() == null)
			max = (BigDecimal) type.getMax();
		else {
			BigDecimal entry = (BigDecimal) ranges.getStrongTypedMax();
			if (ranges.includesMaximum())
				max = entry;
			else
				max = entry.subtract((BigDecimal) type.getAtom());
		}

		return min.add(new BigDecimal(rng.nextDouble()).multiply(max
				.subtract(min)));
	}

	public static Object randomshort(short min, short max, Random rng) {
		return min + (rng.nextInt((max - min)));
	}

	public static Long randomlong(long min, long max, Random rng) {
		long diff = max - min;
		if (diff < 0L) {
			BigDecimal bigMin = new BigDecimal(min);
			BigDecimal bigMax = new BigDecimal(max);
			BigDecimal diffL = bigMax.subtract(bigMin);
			return bigMin.add(new BigDecimal(rng.nextDouble()).multiply(diffL))
					.longValue();
		} else
			return min + (long) (rng.nextDouble() * diff);
	}

	public static Double randomdouble(double min, double max, Random rng) {
		return min + (rng.nextDouble() * (max - min));
	}

	public static Float randomfloat(float min, float max, Random rng) {
		return min + (rng.nextFloat() * (max - min));
	}

	public static Integer randominteger(int min, int max, Random rng) {
		long diff = (long) max - (long) min;
		if (diff > Integer.MAX_VALUE) {
			int halfdiff = (int) (diff / 2) - (rng.nextBoolean() ? 1 : 0);
			return min + ((int) (rng.nextDouble() * halfdiff) * 2);
		} else
			return min + (int) (rng.nextDouble() * diff);
	}

	public Comparable<?> parse(String expression) {
		Comparable<?> result = null;
		switch (this) {
		case DECIMAL:
			result = new BigDecimal(expression);
			break;
		case DOUBLE:
			result = Double.parseDouble(expression);
			break;
		case FLOAT:
			result = Float.parseFloat(expression);
			break;
		case INTEGER:
			result = Integer.parseInt(expression);
			break;
		case SHORT:
			result = Short.parseShort(expression);
			break;
		case LONG:
			result = Long.parseLong(expression);
			break;
		case BOOLEAN:
			result = Boolean.parseBoolean(expression);
			break;
		}
		return result;
	}

	public static boolean isNumeric(String identifier) {
		String id = identifier.toUpperCase();
		boolean flag = true;
		try {
			valueOf(Numeric.class, id);
		} catch (IllegalArgumentException e) {
			flag = false;
		}
		return flag;
	}
}