package se.miun.itm.input.tuning.sequential.spot;

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

	public String toSpot() {
		StringBuilder spotb = new StringBuilder();
		String spotHeader = headerToSpot();
		spotb.append(spotHeader);
		spotb.append('\n');
		for (SpotResult result : allResults) {
			spotb.append(result.toSpot());
			spotb.append('\n');
		}
		return spotb.toString();
	}

	@Override
	public String toString() {
		return toSpot();
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
		return new ByteArrayInputStream(toSpot().getBytes());
	}

}
