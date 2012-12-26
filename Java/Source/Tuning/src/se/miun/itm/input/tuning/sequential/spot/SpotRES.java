package se.miun.itm.input.tuning.sequential.spot;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public class SpotRES implements SpotExportable<InputStream> {

	private final List<SpotResult> results;

	private SpotROI output;

	private SpotROI input;

	private final List<String> header;

	public SpotRES(List<IExperiment> results, SpotDES currentDES,
			SpotROI input, SpotROI output) throws InPUTException {
		this.input = input;
		this.output = output;
		header = initHeader();
		this.results = initResults(results, currentDES);
	}

	private List<String> initHeader() {
		List<String> entries = new ArrayList<String>();
//		appendParamIds(output, entries);
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

	private List<SpotResult> initResults(List<IExperiment> inputResults,
			SpotDES currentDES) throws InPUTException {
		List<SpotResult> results = new ArrayList<SpotResult>();
		append(inputResults, currentDES, results);
		return results;
	}

	private void append(List<IExperiment> from, SpotDES currentDES,
			List<SpotResult> to) throws InPUTException {
		SpotResult result;
		SpotDesign design;
		int designCounter = 0;
		for (IExperiment experiment : from) {
			design = currentDES.getDesign(designCounter);
			for (IDesign output : experiment.getOutput()) {
				result = new SpotResult(output, header, design);
				to.add(result);
				designCounter++;
			}
		}
	}

	public void append(List<IExperiment> inputResults, SpotDES currentDES)
			throws InPUTException {
		append(inputResults, currentDES, results);
	}

	public StringBuilder toSpot() {
		StringBuilder spotb = new StringBuilder();
		String spotHeader = headerToSpot();
		spotb.append(spotHeader);
		spotb.append('\n');
		for (SpotResult result : results) {
			spotb.append(result.toSpot());
			spotb.append('\n');
		}
		return spotb;
	}

	@Override
	public String toString() {
		return toSpot().toString();
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
		return new ByteArrayInputStream(toSpot().toString().getBytes());
	}

}
