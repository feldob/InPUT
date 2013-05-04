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
 */package se.miun.itm.input.tuning.sequential.spot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpotDesign {

	private final List<String> values;
	private final Map<String, Integer> map;
	private final SpotROI roi;
	private String[] paramIdsNoConfig;
	
	public SpotDesign(SpotROI roi , String[] paramIds, String desLine) {
		this.roi = roi;
		paramIdsNoConfig = Arrays.copyOfRange(paramIds, 0, paramIds.length-4);
		map = new HashMap<String, Integer>();
		values = initValues(paramIds, desLine);
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

	public String[] getParamIds() {
		return paramIdsNoConfig;
	}
	
}
