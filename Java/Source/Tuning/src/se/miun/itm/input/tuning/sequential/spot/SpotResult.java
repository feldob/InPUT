package se.miun.itm.input.tuning.sequential.spot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public class SpotResult {

	private final Map<String, String> variables;
	
	private final List<String> header;

	public SpotResult(IDesign output, List<String> header, SpotDesign design)
			throws InPUTException {
		this.header = header;
		variables = new HashMap<String, String>();
		initVariables(output, design);
	}

	private void initVariables(IDesign output, SpotDesign design) throws InPUTException {
		String functionId, value = null;
		functionId = design.getRoi().getFunctionId();
		for (String var : header) {
			if (var.equals(SPOTQ.ATTR_AMOUNT_DIMENSIONS)) {
				value = calculateDimensionsOfStudy(header) + "";
			} else if (var.equals(SPOTQ.ATTR_ALGORITHM_ID))
				value = functionId;
			else if (design.containsVariable(var))
				value = design.getValueFor(var);
			else if(var.equals(SPOTQ.Y))
				value = output.getValue(var).toString();
			
			if (value!= null)
				variables.put(var, value);
		}
	}

	private int calculateDimensionsOfStudy(List<String> header) {
		return header.size() - 6;
	}

	public String getValueFor(String var) {
		return variables.get(var);
	}

	public String toSpot() {
		StringBuilder b = new StringBuilder();
		for (String var : header) {
			b.append(variables.get(var));
			b.append(' ');
		}
		return b.toString();
	}
	
	@Override
	public String toString() {
		return toSpot();
	}
}