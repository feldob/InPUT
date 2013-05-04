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
 *//*-- $Copyright (C) 2012 Felix Dobslaw$

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
import java.util.regex.Pattern;

import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.util.Q;

/**
 * Numeric is the service enum that supplies lower and upper extrema for all
 * numeric values, and supports the random creation of numeric values of all
 * supported kinds, given range restrictions.
 * 
 * @author Felix Dobslaw
 * 
 *  @ThreadSafe
 */
public enum Numeric {

	BOOLEAN(Boolean.class, Boolean.TYPE, "Boolean", Boolean.FALSE,
			Boolean.TRUE, true), INTEGER(Integer.class, Integer.TYPE, "Int",
			Integer.MIN_VALUE, Integer.MAX_VALUE, 1), LONG(Long.class,
			Long.TYPE, "Long", Long.MIN_VALUE, Long.MAX_VALUE, 1L), FLOAT(
			Float.class, Float.TYPE, "Float", Float.MIN_VALUE, 1f,
			0f), DOUBLE(Double.class, Double.TYPE, "Double",
			0d, 1d, Double.MIN_VALUE), DECIMAL(BigDecimal.class,
			BigDecimal.class, "Decimal", new BigDecimal(Long.MIN_VALUE),
			new BigDecimal(Long.MAX_VALUE), new BigDecimal(Double.MIN_VALUE)), SHORT(
			Short.class, Short.TYPE, "Short", Short.MIN_VALUE, Short.MAX_VALUE,
			1);

	private final Class<?> numClass;
	private final Class<?> numPrimClass;
	private final String className;
	private final Comparable<?> atom;
	private final Comparable<?> min;
	private final Comparable<?> max;

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

	public Object random(Ranges ranges, Random rng) {
		
		Comparable<?>[] min = ranges.getStrongTypedMin();
		Comparable<?>[] max = ranges.getStrongTypedMax();
		
		int i = rng.nextInt(min.length);
		
			switch (this) {
			case DECIMAL:
				return randomdecimal(ranges, (BigDecimal) min[i], (BigDecimal) max[i], rng);
			case INTEGER:
				return randominteger((Integer) min[i],
						(Integer) max[i], rng);
			case LONG:
				return randomlong((Long) min[i],
						(Long) max[i], rng);
			case FLOAT:
				return randomfloat((Float) min[i],
						(Float) max[i], rng);
			case DOUBLE:
				return randomdouble((Double) min[i],
						(Double) max[i], rng);
			case BOOLEAN:
				return rng.nextBoolean();
			case SHORT:
				return randomshort((Short) min[i],
						(Short) max[i], rng);
		}
		return null;

	}

	private BigDecimal randomdecimal(Ranges ranges, BigDecimal min, BigDecimal max, Random rng) {
//		BigDecimal min, max;
//
//		if (ranges.getMinExpression() == null)
//			min = (BigDecimal) getMin();
//		else {
//			BigDecimal entry = (BigDecimal) ranges.getStrongTypedMin();
//			if (ranges.includesMinimum())
//				min = entry;
//			else
//				min = entry.add((BigDecimal) getAtom());
//		}
//
//		if (ranges.getMaxExpression() == null)
//			max = (BigDecimal) getMax();
//		else {
//			BigDecimal entry = (BigDecimal) ranges.getStrongTypedMax();
//			if (ranges.includesMaximum())
//				max = entry;
//			else
//				max = entry.subtract((BigDecimal) getAtom());
//		}

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

	public Comparable<?>[] parse(String expression) {
		String[] values = expression.split(Pattern.quote(","));
		
		Comparable<?>[] results = new Comparable<?>[values.length];
			for (int i = 0; i < values.length; i++) {
				results[i] = parseSingle(values[i]);
			}
		
		return results;
	}

	public Comparable<?> parseSingle(String expression) {
		Comparable<?> result = null;
		switch (this) {
		case DECIMAL:
			result = new BigDecimal(expression);
			break;
		case DOUBLE:
			result = new BigDecimal(expression).doubleValue();
			break;
		case FLOAT:
			result = new BigDecimal(expression).floatValue();
			break;
		case INTEGER:
			result = new BigDecimal(expression).intValue();
			break;
		case SHORT:
			result = new BigDecimal(expression).shortValue();
			break;
		case LONG:
			result = new BigDecimal(expression).longValue();
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

	public static Numeric valueOf(NParam param) {
		return valueOf(param.getAttributeValue(Q.TYPE_ATTR).split(
				Q.ESCAPED_ARRAY_START)[0].toUpperCase());
	}

	@SuppressWarnings("incomplete-switch")
	public boolean isCountable() {
		boolean countable = false;
		switch (this) {
		case INTEGER:
		case SHORT:
		case LONG:
		case BOOLEAN:
			countable = true;
		}
		return countable;
	}
}