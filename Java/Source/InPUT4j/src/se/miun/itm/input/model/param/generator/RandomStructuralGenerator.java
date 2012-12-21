package se.miun.itm.input.model.param.generator;

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