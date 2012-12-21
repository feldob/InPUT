package se.miun.itm.input.tuning.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.tuning.sequential.spot.SpotDES;
import se.miun.itm.input.tuning.sequential.spot.SpotRES;
import se.miun.itm.input.tuning.sequential.spot.SpotROI;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

public class SpotConverter implements InputConverter<SpotDES, SpotROI, SpotRES> {

	public static final String ID_CHOP_PATTERN = Pattern.quote(".");

	private SpotDesignInitializer experimentInitializer;

	private int designId;
	
	public SpotConverter() {
		experimentInitializer = new SpotDesignInitializer();
		designId = 1;
	}

	@Override
	public SpotDES fromExperiments(List<IExperiment> designs) throws InPUTException {
		if (designs == null)
			throw new InPUTException("No designs given.");
		if (designs.isEmpty())
			return null;

		return new SpotDES(designs);
	}

	@Override
	public SpotROI fromDesignSpace(IDesignSpace inputSpace)
			throws InPUTException {
		if (inputSpace == null)
			throw new InPUTException("No space given.");

		return new SpotROI(inputSpace);
	}

	@Override
	public List<IExperiment> toExperiments(String inputId, SpotDES experimentalDesign)
			throws InPUTException {
		if (experimentalDesign == null)
			throw new InPUTException("No design given.");

		IInPUT input = InPUT.lookup(inputId);

		try {
			return toExperiments(experimentalDesign, input);
		} catch (IOException e) {
			throw new InPUTException(
					"Something went wrong while reading the SPOT design file.");
		}
	}

	private List<IExperiment> toExperiments(SpotDES des, IInPUT input)
			throws IOException, InPUTException {
		List<IExperiment> experiments = new ArrayList<IExperiment>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				des.toInputStream()));
		String[] paramIds = getDeclaredContent(reader.readLine());

		List<Param<?>> fixed = getFixed(input);
		String fixedValueString = createValueString(des, fixed);
		paramIds = appendFixedParams(des, fixed, paramIds);

		IExperiment experiment = null;
		Map<Integer, Set<ParamValuePair>> values;
		String experimentSequence;
		while (reader.ready()) {
			experimentSequence = fixedValueString + reader.readLine();
				values = retrieveParameterValues(paramIds, experimentSequence);
				experiment = experimentInitializer.initExperiment(designId, values, input);
				designId++;
			experiments.add(experiment);
		}
		return experiments;
	}

	private List<Param<?>> getFixed(IInPUT input) {
		List<Param<?>> fixed = new ArrayList<Param<?>>();
		appendFixed(fixed, input.getAlgorithmDesignSpace());
		appendFixed(fixed, input.getPropertySpace());
		return fixed;
	}

	private void appendFixed(List<Param<?>> fixed,
			IDesignSpace aSpace) {
		if (aSpace == null)
			return;
		
		ParamStore store = ParamStore.getInstance(aSpace.getId());
		
		fixed.addAll(store.getFixed());
	}

	private String createValueString(SpotDES des, List<Param<?>> fixed) throws InPUTException {
		StringBuilder b = new StringBuilder();
		if (fixed.size() > 0)
			for (Param<?> param : fixed) {
				if (!des.getDesign(0).containsVariable(param.getId())) {
					appendFixedValue(b, param);
				}
			}
		return b.toString();
	}

	private void appendFixedValue(StringBuilder b, Param<?> param) throws InPUTException {
		String fixed = param.getFixedValue();
		if (param instanceof SParam && ((SParam)param).isComplex()) {
			String[] fixedChops = fixed.split(" ");
			String chop;
			for (int i = 0; i < fixedChops.length; i++) {
				chop = fixedChops[i];
				if (!chop.equals(Q.PLACEHOLDER)) {
					b.append(chop);
					b.append(' ');					
				}
			}
		}else{
			b.append(fixed);
			b.append(' ');
		}
	}

	private String[] appendFixedParams(SpotDES des, List<Param<?>> fixed,
			String[] paramIds) throws InPUTException {
		String[] allParams = paramIds;

		if (fixed.size() > 0) {
			List<String> fixedRelevantIds = new ArrayList<String>();

			for (Param<?> fixedId : fixed) {
				if (!des.getDesign(0).containsVariable(fixedId.getId())) {
					addFixedRelevantParamIds(fixedRelevantIds, fixedId);
				}
			}
			if (fixedRelevantIds.size() > 0) {
				allParams = ParamUtil
						.concat(fixedRelevantIds
								.toArray(new String[fixedRelevantIds.size()]),
								paramIds);
			}
		}
		return allParams;
	}

	private void addFixedRelevantParamIds(List<String> fixedRelevantIds, Param<?> fixedId) throws InPUTException {
		if (fixedId instanceof SParam && ((SParam)fixedId).isComplex()) {
			String[] fixed = fixedId.getFixedValue().split(" ");
			for (int i = 0; i < fixed.length; i++)
				if (!fixed[i].equals(Q.PLACEHOLDER))
					fixedRelevantIds.add(fixedId.getId() + '.' + (i+1));
		}else
			fixedRelevantIds.add(fixedId.getId());
	}

	private String[] getDeclaredContent(String design) throws IOException {
		String[] allResults = design.split(" ");
		return Arrays.copyOf(allResults, allResults.length - 4);
	}

	private Map<Integer, Set<ParamValuePair>> retrieveParameterValues(
			String[] paramIds, String design) throws IOException {
		Map<Integer, Set<ParamValuePair>> paramMap = new HashMap<Integer, Set<ParamValuePair>>();

		String[] values = getDeclaredContent(design);

		int depth;
		for (int i = 0; i < values.length; i++) {
			depth = paramIds[i].split(ID_CHOP_PATTERN).length;
			if (!paramMap.containsKey(depth))
				paramMap.put(depth, new HashSet<ParamValuePair>());
			paramMap.get(depth).add(new ParamValuePair(paramIds[i], values[i]));
		}

		return paramMap;

	}

	@Override
	public IDesignSpace toDesignSpace(SpotROI designSpace)
			throws InPUTException {
		if (designSpace == null)
			throw new InPUTException("No design space given.");
		return null;
	}

	@Override
	public SpotRES fromResults(List<IExperiment> results) {
		// StringBuilder res = new StringBuilder();
		// StringBuilder header = extractHeader(results.get(0).getInPUT());
		//
		// res.append(header);
		// for (IExperiment experiment : results) {
		// res.append(fromResult(experiment));
		// }
		return null;
	}

	public void copyOrderedIds(StringBuilder builder, IDesignSpace space) {
		List<String> ids = new ArrayList<String>(space.getSupportedParamIds());
		Collections.sort(ids);

		for (String id : ids) {
			builder.append(id);
			builder.append(' ');
		}
	}

}