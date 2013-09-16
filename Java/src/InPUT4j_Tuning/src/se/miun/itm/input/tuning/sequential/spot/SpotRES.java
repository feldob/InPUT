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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public class SpotRES implements SpotExportable<InputStream> {

	private final List<SpotResult> results = new ArrayList<SpotResult>();

	private SpotROI input;

	private final List<String> header;

	private SpotDES currentDES;
	
	private List<SpotResult> allResults = new ArrayList<SpotResult>();

	public SpotRES(SpotROI input, SpotROI output) throws InPUTException {
		this.input = input;
		header = initHeader();
	}

	private List<String> initHeader() {
		List<String> entries = new ArrayList<String>();
		entries.add(SPOTQ.Y);
		appendParamIds(input, entries);
		appendSpotSpecificIds(entries);
		return entries;
	}

	private void appendParamIds(SpotROI roi, List<String> entries) {
		for (SpotParam param : roi.getParams())
			entries.add(param.id);
	}

	private void appendSpotSpecificIds(List<String> entries) {
		entries.add(SPOTQ.ATTR_ALGORITHM_ID);
		entries.add(SPOTQ.ATTR_AMOUNT_DIMENSIONS);
		entries.add(SPOTQ.ATTR_SEED);
		entries.add(SPOTQ.ATTR_CONFIGURATION_NUMBER);
		entries.add(SPOTQ.ATTR_ITERATION);
	}

	private void append(IDesign output, SpotDES currentDES, List<SpotResult> to) throws InPUTException {
		if (this.currentDES == null || !currentDES.equals(this.currentDES)){
			to.clear();
			this.currentDES = currentDES;
		}
			
		SpotDesign design = currentDES.getDesign(to.size());
		SpotResult result = new SpotResult(output, header, design);
		allResults.add(result);
		to.add(result);
	}

	public void append(IDesign result, SpotDES currentDES) throws InPUTException {
		append(result, currentDES, results);
	}

	public String toSpot(List<SpotResult> results) {
		StringBuilder spotb = new StringBuilder();
		String spotHeader = headerToSpot();
		spotb.append(spotHeader);
		spotb.append('\n');
		for (SpotResult result : results)
			spotb.append(result.toSpot());
		return spotb.toString();
	}

	@Override
	public String toString() {
		return toSpot(allResults);
	}

	private String headerToSpot() {
		StringBuilder b = new StringBuilder();
		for (String var : header) {
			b.append(var);
			b.append(' ');
		}
		return b.toString();
	}

	@Override
	public InputStream export() {
		return new ByteArrayInputStream(toSpot(allResults).getBytes());
	}

	public SpotResult getLastResult() {
		return allResults.get(allResults.size()-1);
	}

	public boolean isEmpty() {
		return allResults.isEmpty();
	}

	public String currentResultstoString() {
		return toSpot(results);
	}
}
