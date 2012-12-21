package se.miun.itm.input.tuning.sequential.spot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpotDesign {

	private final List<String> values;
	private final Map<String, Integer> map;
	private final SpotROI roi;
	
	public SpotDesign(SpotROI roi , String[] params, String desLine) {
		this.roi = roi;
		map = new HashMap<String, Integer>();
		values = initValues(params, desLine);
	}
	
	private List<String> initValues(String[] params, String desLine) {
		List<String> values = new ArrayList<String>();
		String[] vals = desLine.split(" ");
		for (int i = 0; i < vals.length; i++)
		{
			values.add(vals[i].trim());
			map.put(params[i], i);
		}
		return Collections.unmodifiableList(values);
	}

	public String getValueFor(String paramId) {
		return values.get(map.get(paramId));
	}
	
	public String getValueAt(int position) {
		return values.get(position);
	}
	
	public int size() {
		return values.size();
	}

	public boolean containsVariable(String var) {
		return map.containsKey(var);
	}

	public SpotROI getRoi() {
		return roi;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (String var : values) {
			b.append(var);
			b.append(' ');
		}
		return b.toString();
	}
	
}
