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
 */package se.miun.itm.input.export;

import java.util.regex.Pattern;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.util.Q;

public class SpotExportHelper {

	private static final char LINEBREAK = '\n';
	
	private static final String SPOT_NON_COUNTABLE = "FLOAT";
	private static final String SPOT_COUNTABLE = "INT";
	private static final String SPOT_FACTOR = "FACTOR";

	public SpotExportHelper() {
	}

	public void appendParam(StringBuilder builder, Param<?> param)
			throws InPUTException {

		String fixed = param.getFixedValue();
		if (fixed == null || fixed.contains(" ")) {
			if (param instanceof NParam) {
				appendNumeric(builder, (NParam) param);
			} else if (param instanceof SParam) {
				SParam sParam = (SParam) param;
				if (sParam.isComplex())
					appendComplex(builder, sParam, fixed);
				else
					appendStructual(builder, sParam);
			}
		}

		appendChildren(builder, param, fixed);
	}

	private void appendComplex(StringBuilder builder, SParam complex, String fixed)
			throws InPUTException {
		int[] dims = complex.getDimensions();
		if (dims.length == 0 || dims[0] < 1 || dims.length > 1)
			throw new InPUTException(
					complex.getId()
							+ ": For a valid experimental design, complex parameters have to be one-dimensional, [x], with x > 0");

		int dimensions = dims[0];
		
		String[] fixedEntries = getFixedChops(fixed);
		for (int i = 0; i < dimensions; i++)
			if (isEligible(fixedEntries, i))
				appendComplexPosition(builder, complex, i);
	}

	private String[] getFixedChops(String fixed) {
		if (fixed == null)
			return null;
		
		String[] fixedEntries = fixed.split(Pattern.quote(" "));
		return fixedEntries;
	}

	private boolean isEligible(String[] fixed, int i) {
		if (fixed == null)
			return true;
		
		if (i < fixed.length && !fixed[i].equals(Q.PLACEHOLDER))
			return false;
		
		return true;
	}

	private void appendComplexPosition(StringBuilder builder, SParam complex,
			int i) {
		appendComplexPositionName(builder, complex, i);
		appendComplexPositionLow(builder);
		appendComplexPositionHigh(builder, complex);
		appendType(builder, complex);
	}

	private void appendComplexPositionHigh(StringBuilder builder, SParam complex) {
		builder.append(complex.getAmountChoices());
		builder.append(" ");
	}

	private void appendComplexPositionLow(StringBuilder builder) {
		builder.append("1 ");
	}

	public void appendComplexPositionName(StringBuilder builder,
			SParam complex, int i) {
		builder.append(complex.getId());
		builder.append(".");
		builder.append("" + (i + 1));
		builder.append(" ");
	}

	private void appendChildren(StringBuilder builder, Param<?> param,
			String fixed) throws InPUTException {
		for (Element childParam : param.getChildren())
			if (childParam instanceof Param<?>)
				if (notEffectedByFixedChoice(fixed, (Param<?>) childParam))
					appendParam(builder, (Param<?>) childParam);
	}

	public boolean notEffectedByFixedChoice(String fixed, Param<?> childParam) {
		return fixed == null || !(childParam instanceof SChoice)
				|| childParam.getLocalId().equals(fixed) || fixed.contains(" ");
	}

	private void appendStructual(StringBuilder builder, SParam param) {
		appendName(builder, param);
		appendLow(builder, param);
		appendHigh(builder, param);
		appendType(builder, param);
	}

	private void appendType(StringBuilder builder, SParam param) {
		builder.append(SPOT_FACTOR);
		builder.append(LINEBREAK);
	}

	private void appendLow(StringBuilder builder, SParam param) {
		builder.append("1 ");
	}

	private void appendHigh(StringBuilder builder, SParam param) {
		int high = param.getAmountChoices();
		if (high == 0)
			high = 1;
		builder.append(high + " ");
	}

	private void appendNumeric(StringBuilder builder, NParam param) throws InPUTException {
		appendName(builder, param);
		appendLow(builder, param);
		appendHigh(builder, param);
		appendType(builder, param);
	}

	private void appendName(StringBuilder builder, Param<?> param) {
		append(builder, param.getId());
	}

	private void append(StringBuilder builder, String value) {
		builder.append(value + " ");
	}

	private void appendHigh(StringBuilder builder, NParam param) throws InPUTException {
		append(builder, param.getNumericMaxValue());
	}

	private void appendLow(StringBuilder builder, NParam param) throws InPUTException {
		append(builder, param.getNumericMinValue());
	}

	private void appendType(StringBuilder builder, NParam param) {
		String type = getSpotNumericType(param);
		builder.append(type + "\n");
	}

	private String getSpotNumericType(NParam param) {
		if (param.isBoolean())
			return SPOT_FACTOR;
		if (param.isCountable())
			return SPOT_COUNTABLE;
		return SPOT_NON_COUNTABLE;
	}

}
