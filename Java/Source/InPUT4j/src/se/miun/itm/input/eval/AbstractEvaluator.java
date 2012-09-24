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

import se.miun.itm.input.model.Numeric;

/**
 * ensures for the supported primitive types that they hold an appropriate
 * format. Some of the evaluation engines do not support this (e.g. they return
 * a 1.0 as an integer, which breaks the integer parsing).
 * 
 * @author Felix Dobslaw
 * 
 */
public abstract class AbstractEvaluator implements InputEvaluator {

	protected static String ensureType(Numeric valueType, String evaluate) {
		switch (valueType) {
		case INTEGER:
			evaluate = "" + new Double(evaluate).intValue();
			break;
		case SHORT:
			evaluate = "" + new Double(evaluate).shortValue();
			break;
		case LONG:
			evaluate = "" + new Double(evaluate).longValue();
			break;
		}
		return evaluate;
	}
}