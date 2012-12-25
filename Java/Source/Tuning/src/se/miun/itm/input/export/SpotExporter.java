package se.miun.itm.input.export;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.util.Q;

public class SpotExporter implements Exporter<InputStream> {

	private SpotExportHelper helper;

	public SpotExporter() {
		helper = new SpotExportHelper();
	}

	@Override
	public InputStream export(Document xml) throws InPUTException {
		InputStream is = null;
		String type = xml.getRootElement().getName();
		if (type.equals(Q.DESIGN_SPACE_ROOT)) {
			is = exportDesignSpace(xml);
		} else if (type.equals(Q.DESIGN_ROOT)) {
			is = exportDesign(xml);
		}
		return is;
	}

	private InputStream exportDesignSpace(Document space) throws InPUTException {
		StringBuilder builder = new StringBuilder("name low high type\n");

		List<Param<?>> params = extractSortedFirstLevelParameters(space);
		for (Param<?> param : params)
				helper.appendParam(builder, param);

		return new ByteArrayInputStream(builder.toString().getBytes());
	}

	private List<Param<?>> extractSortedFirstLevelParameters(Document space) {
		ParamStore store = ParamStore.getInstance(space.getRootElement()
				.getAttributeValue(Q.ID_ATTR));

		List<String> paramIds = extractSortedParameterIds(store);

		return getParametersById(paramIds, store);
	}

	private List<String> extractSortedParameterIds(ParamStore store) {
		List<String> paramIds = new ArrayList<String>(
				store.getAllParameterIds());
		Collections.sort(paramIds);
		return paramIds;
	}

	private List<Param<?>> getParametersById(List<String> paramIds,
			ParamStore store) {
		List<Param<?>> params = new ArrayList<Param<?>>(paramIds.size());

		for (String paramId : paramIds)
			if (!paramId.contains("."))
				params.add(store.getParam(paramId));

		return params;
	}

	private InputStream exportDesign(Document design) {
		throw new IllegalArgumentException("Exporting designs via the SpotExporter is a feature not supported yet.");
	}

	@Override
	public InputStream export(Exportable input) throws InPUTException {
		InputStream is = null;
		if (input instanceof IExperiment) {
			is = exportExperiment((IExperiment) input);
		} else if (input instanceof IInPUT) {
			is = exportInput((IInPUT) input);
		}
		return is;
	}

	private InputStream exportExperiment(IExperiment experiment)
			throws InPUTException {

		InputStream spaceStream = experiment.getAlgorithmDesign().export(this);

		InputStream result = spaceStream;
		IDesign preferences = experiment.getPreferences();
		if (preferences != null){
				result = concatDESTables(spaceStream, preferences.export(this));
		}

		return result;
	}

	private InputStream exportInput(IInPUT input) throws InPUTException {
		
		InputStream spaceStream = input.getAlgorithmDesignSpace().export(this);

		InputStream result = spaceStream;
		IDesignSpace preferences = input.getPropertySpace();
		if (preferences != null){
				result = concatROITables(spaceStream, preferences.export(this));
		}

		return result;
	}

	private InputStream concatROITables(InputStream aSpace,
			InputStream bSpace) throws InPUTException {
		StringBuilder builder = new StringBuilder();
		BufferedReader aReader = new BufferedReader(new InputStreamReader(
				aSpace));
		BufferedReader bReader = new BufferedReader(new InputStreamReader(
				bSpace));
		
		try {
			
			builder.append(aReader.readLine());
			builder.append('\n');
			bReader.readLine();
			
			appendROI(builder, aReader);
			appendROI(builder, bReader);
			
		} catch (IOException e) {
			throw new InPUTException(e.getMessage(), e);
		}
		return new ByteArrayInputStream(builder.toString().getBytes());
	}

	private void appendROI(StringBuilder builder, BufferedReader aReader)
			throws IOException {
		while (aReader.ready()) {
			builder.append(aReader.readLine());
			builder.append('\n');
		}
	}

	private InputStream concatDESTables(InputStream aDesign, InputStream bDesign)
			throws InPUTException {
		StringBuilder builder = new StringBuilder();
		BufferedReader aReader = new BufferedReader(new InputStreamReader(
				aDesign));
		BufferedReader bReader = new BufferedReader(new InputStreamReader(
				aDesign));
		try {
			while (aReader.ready()) {
				builder.append(aReader.readLine());
				builder.append(' ');
				builder.append(bReader.readLine());
				builder.append('\n');
			}
		} catch (IOException e) {
			throw new InPUTException(e.getMessage(), e);
		}
		return new ByteArrayInputStream(builder.toString().getBytes());
	}

	@Override
	public String getInfo() {
		return "An exporter that returns SPOT compatible experimental files.";
	}
}