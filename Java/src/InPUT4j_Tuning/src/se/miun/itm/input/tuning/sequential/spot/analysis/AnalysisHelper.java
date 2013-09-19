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
 */package se.miun.itm.input.tuning.sequential.spot.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.tuning.converter.SpotDesignInitializer;
import se.miun.itm.input.tuning.sequential.spot.SPOTQ;
import se.miun.itm.input.util.Q;

public class AnalysisHelper {

	/**
	 * introduces N/A entries for those randomly selected entries in the experimental SPOT design which are not used because they are
	 * hierarchically on an unconsidered branch.
	 * 
	 * @param resultFile
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws InPUTException
	 */
	public static StringBuilder prepareForStatisticalAnalysis(InputStream resultFile, IInPUT input) throws IOException, InPUTException {
		BufferedReader r = new BufferedReader(new InputStreamReader(resultFile));
		String[] header = r.readLine().split(" ");
		List<Map<String, String>> samples = extractSamples(r, header);

		ParamStore ps1 = ParamStore.getInstance(input.getProblemFeatureSpace().getId());
		ParamStore ps2 = ParamStore.getInstance(input.getPropertySpace().getId());
		ParamStore ps3 = ParamStore.getInstance(input.getAlgorithmDesignSpace().getId());

		for (Map<String, String> sample : samples) {
			for (String key : header) {
				if (!isSpecialEntry(key) && isNotInDesign(key, sample, ps1, ps2, ps3)) {
					sample.put(key, SPOTQ.NULL);
				}
			}
		}

		return reassembleResults(header, samples);
	}

	private static StringBuilder reassembleResults(String[] header, List<Map<String, String>> samples) {
		StringBuilder b = new StringBuilder();
		initHeader(header, b);
		for (Map<String, String> sample : samples) {
			initSample(header, sample, b);
		}

		return b;
	}

	private static void initSample(String[] header, Map<String, String> sample, StringBuilder b) {
		for (int i = 0; i < header.length; i++) {
			b.append(sample.get(header[i]));
			b.append(' ');
		}
		b.append('\n');
	}

	private static void initHeader(String[] header, StringBuilder b) {
		for (int i = 0; i < header.length; i++) {
			b.append(header[i]);
			b.append(' ');
		}
		b.append('\n');
	}

	private static boolean isNotInDesign(String key, Map<String, String> sample, ParamStore ps1, ParamStore ps2, ParamStore ps3)
			throws InPUTException {

		Param<?> p = collectParam(key, ps1, ps2, ps3);
		if (p == null && key.matches(".*\\d.*")) {
			String choiceNumber = sample.get(key);
			Param<?> complexParent = collectParam(key.substring(0, key.lastIndexOf('.')), ps1, ps2, ps3);
			p = ((SParam) complexParent).getChoiceByPosition((int) Double.parseDouble(choiceNumber));
		}

		if (p == null)
			throw new IllegalArgumentException("The parameter does not exist: " + key);

		return !isParentInDesign(p, sample);
	}

	private static boolean isParentInDesign(Param<?> p, Map<String, String> sample) throws InPUTException {

		Element parent = p.getParentElement();
		if (parent.isRootElement())
			return true;

		boolean flag = false;
		if (parent instanceof SParam || parent instanceof NParam) {
			if (SpotDesignInitializer.isStructuralArrayType(parent) && p instanceof SChoice) {
				String compl, choiceNumber;
				for (int j = 1; j <= ((SParam) parent).getDimensions()[0]; j++) {
					compl = p.getParamId() + "." + j;
					choiceNumber = sample.get(compl);
					if (isRelevantComplexChoice((SChoice) p, sample, (SParam) parent, compl, choiceNumber)) {
						flag = true;
						break;
					}
				}
			} else
				flag = isParentInDesign((Param<?>) parent, sample);
		} else if (parent instanceof SChoice) {
			flag = checkChoice(sample, parent, flag);
		}

		return flag;
	}

	private static boolean isRelevantComplexChoice(SChoice choice, Map<String, String> sample, SParam complex, String compl,
			String choiceNumber) throws NumberFormatException, InPUTException {
		return isFixedComplexChoice(choice, complex)
				|| (sample.containsKey(compl) && complex.getChoiceByPosition((int) Double.parseDouble(choiceNumber)).equals(choice));
	}

	private static boolean isFixedComplexChoice(SChoice p, SParam parent) {
		String fixed = parent.getAttributeValue(Q.FIXED_ATTR);
		String[] entries;
		if (fixed != null) {
			entries = fixed.split(" ");
			for (String fix : entries) {
				if (fix.equals(p.getLocalId())) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean checkChoice(Map<String, String> sample, Element parent, boolean flag) throws InPUTException {
		SChoice choice = (SChoice) parent;
		SParam choiceParent = (SParam) choice.getParent();
		String choiceNumber = sample.get(choiceParent.getId());

		if (choiceNumber == null) {
			if (choiceParent.isRootElement()) {
				flag = true;
			} else if (choiceParent.isComplex())
				flag = isParentInDesign(choice, sample);
			else
				flag = isParentInDesign(choiceParent, sample);
		} else if (choiceNumber.equals(SPOTQ.NULL)) {
			flag = false;
		} else if (choiceParent.getChoiceByPosition((int) Double.parseDouble(choiceNumber)).equals(choice)) {
			flag = true;
		}
		return flag;
	}

	private static Param<?> collectParam(String paramId, ParamStore ps1, ParamStore ps2, ParamStore ps3) {
		Param<?> param = null;
		if (ps1.containsParam(paramId))
			param = ps1.getParam(paramId);
		else if (ps2.containsParam(paramId))
			param = ps2.getParam(paramId);
		else if (ps3.containsParam(paramId))
			param = ps3.getParam(paramId);
		return param;
	}

	private static boolean isSpecialEntry(String key) {
		if (key.equals(SPOTQ.Y) || key.equals(SPOTQ.ATTR_SEED) || key.equals(SPOTQ.ATTR_AMOUNT_DIMENSIONS)
				|| key.equals(SPOTQ.ATTR_ALGORITHM_ID) || key.equals(SPOTQ.ATTR_CONFIGURATION_NUMBER) || key.equals(SPOTQ.ATTR_ITERATION))
			return true;
		return false;
	}

	private static List<Map<String, String>> extractSamples(BufferedReader r, String[] headers) throws IOException {
		Map<String, String> sample;

		List<Map<String, String>> samples = new ArrayList<Map<String, String>>();
		String[] values;
		while (r.ready()) {
			sample = new HashMap<String, String>();
			values = r.readLine().split(" ");
			for (int i = 0; i < values.length; i++) {
				sample.put(headers[i], values[i]);
			}
			samples.add(sample);
		}
		return samples;
	}
}
