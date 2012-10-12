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
package se.miun.itm.input.model.element;

import java.util.Map;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;

/**
 * The single service point that helps to create appropriate value elements, depending on their
 * context.
 * 
 * @author Felix Dobslaw
 * 
 */
public class ValueFactory {

	public static Value<? extends Param> constructElementByElement(
			Element original, Param param, Integer[] dimensions,
			ElementCache elementCache) throws InPUTException {
		if (param instanceof SParam)
			return new SValue(original, (SParam) param, dimensions,
					elementCache);
		else if (param instanceof NParam)
			return new NValue(original, (NParam) param, dimensions);
		return null;
	}

	public static Value<? extends Param> constructElementByValue(
			final Object value, final Param param, final Integer[] dimensions,
			final ElementCache elementCache) throws InPUTException {
		if (param instanceof SParam)
			return new SValue(value, (SParam) param, dimensions, elementCache);
		else if (param instanceof NParam)
			return new NValue(value, (NParam) param, dimensions);
		return null;
	}

	public static Value<? extends Param> constructRandomElement(
			final Param param, final Integer[] sizeArray,
			final Map<String, Object> vars, final Object[] actualParams,
			final ElementCache elementCache) throws InPUTException {
		Value<? extends Param> randomE;
		if (param instanceof SParam)
			randomE = new SValue((SParam) param, sizeArray, elementCache);
		else if (param instanceof NParam)
			randomE = new NValue((NParam) param, sizeArray, elementCache);
		else if (param instanceof SChoice)
			randomE = new SValue((SChoice) param, sizeArray, elementCache);
		else
			throw new InPUTException(
					"InPUT cannot recognize the given parameter id to be part of the design space: "
							+ param);

		randomE.initRandom(vars, actualParams, true);
		return randomE;
	}

	public static Value<? extends Param> constructValueElement(
			final Param param, final ElementCache elementCache) throws InPUTException {
		if (param instanceof SParam)
			return new SValue((SParam) param, param.getDimensions(), elementCache);
		else if (param instanceof NParam)
			return new NValue((NParam) param, param.getDimensions(), elementCache);
		else
			throw new InPUTException(
					"The dom element is not of a valid InPUT parameter type.");
	}
}
