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
 */package se.miun.itm.input.model.param.generator;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.XPathProcessor;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public class RandomStructuralGenerator extends StructuralGenerator {

	private List<Element> orderedChoices;

	public RandomStructuralGenerator(SParam param, Random rng)
			throws InPUTException {
		super(param, rng);
	}

	@Override
	public Object parse(String choiceId) throws InPUTException {
		SParam param = ((SParam) this.param);
		AStruct choice = null;
		try {
			int position = Integer.parseInt(choiceId);
			choice = getChoiceAt(position);
		} catch (NumberFormatException e) {
			choice = param.getChoiceById(choiceId);
		}
		return choice.newInstance(null);
	}

	private SChoice getChoiceAt(int position) throws InPUTException {
		if (orderedChoices == null)
			orderedChoices = XPathProcessor.query("SChoice", Q.DESIGN_SPACE_NAMESPACE, param);
		return (SChoice)orderedChoices.get(position-1);
	}

	@Override
	public Object next(Map<String, Object> vars) throws InPUTException {
		return ((SParam) param).nextChoice();
	}

	@Override
	public String getValueForIndex(int index) throws InPUTException {
		AStruct choice = null;
		choice = getChoiceAt(index);
		return choice.getLocalId();
	}
}