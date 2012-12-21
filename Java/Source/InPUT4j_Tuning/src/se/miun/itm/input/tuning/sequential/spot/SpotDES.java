package se.miun.itm.input.tuning.sequential.spot;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.export.SpotExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.InputStreamWrapper;

public class SpotDES {

	private final SpotExporter exporter;

	private final List<SpotDesign> designs;

	private final SpotROI roi;

	private final InputStreamWrapper designStream;

	private SpotDES(Object helper, Object secondHelper, Object thirdHelper)
			throws InPUTException {
		exporter = new SpotExporter();
		roi = initRoi(helper);
		try {
			designStream = initDESStream(helper, secondHelper, thirdHelper,
					exporter);
			designs = initSpotDesigns(designStream);
		} catch (Exception e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	public SpotDES(List<IExperiment> experiments) throws InPUTException {
		this(experiments, null, null);
	}

	public SpotDES(RVector designs, String[] paramIds, SpotROI roi)
			throws InPUTException {
		this(roi, paramIds, designs);
	}

	public SpotDES(String filePath, SpotROI roi) throws InPUTException {
		this(roi, filePath, null);
	}

	private List<SpotDesign> initSpotDesigns(InputStreamWrapper wrapper)
			throws InPUTException, IOException {
		List<SpotDesign> designs = new ArrayList<SpotDesign>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				wrapper.next()));
		String[] params = reader.readLine().split(" ");
		while (reader.ready())
			designs.add(new SpotDesign(roi, params, reader.readLine()));
		return designs;
	}
	
	public SpotDesign getDesign(int position) {
		return designs.get(position);
	}

	@SuppressWarnings("unchecked")
	private static InputStreamWrapper initDESStream(Object helper,
			Object secondHelper, Object thirdHelper, SpotExporter exporter)
			throws InPUTException, IOException {
		InputStream is = null;
		if (helper instanceof List<?>) {
			 is = initDesigns(exporter,
					(List<IDesign>) (List<?>) helper);
		} else if (helper instanceof SpotROI) {
			if (secondHelper != null) {
				if (secondHelper instanceof String[]) {
					is = initDesigns(
							(RVector) thirdHelper, (String[]) secondHelper);
				} else {
					is = new FileInputStream(
							(String) secondHelper);
				}
			}
		}
		return new InputStreamWrapper(is);
	}

	public static InputStream initDesigns(RVector designs, String[] paramIds) {
		StringBuilder b = new StringBuilder();
		appendHeader(paramIds, b);
		appendDesigns(designs, b);
		return new ByteArrayInputStream(b.toString().getBytes());
	}

	public static InputStream initDesigns(SpotExporter exporter,
			List<IDesign> designs) throws InPUTException {
		InputStream is = null;

		if (!designs.isEmpty()) {

			StringBuilder builder = new StringBuilder();

			IDesign design;
			for (int i = 0; i < designs.size(); i++) {
				design = designs.get(i);
				is = design.export(exporter);
				if (i == 0)
					appendHeadline(builder, is);
				appendValues(builder, is);
			}

			is = new ByteArrayInputStream(builder.toString().getBytes());
		}
		return is;
	}

	public SpotROI initRoi(Object helper) throws InPUTException {
		SpotROI roi = null;
		try {
			if (designs instanceof List<?>)
			{
				IDesignSpace space = ((IDesign) designs.get(0)).getSpace(); 
				roi = new SpotROI(space.export(exporter), space.getId());
			}
			else if (helper instanceof SpotROI)
				roi = (SpotROI) helper;
		} catch (FileNotFoundException e) {
			throw new InPUTException("InPUT cannot find the helper file.", e);
		}
		return roi;
	}

	public static void appendHeader(String[] paramIds, StringBuilder b) {
		for (int i = 0; i < paramIds.length; i++) {
			b.append(paramIds[i]);
			b.append(' ');
		}
		b.append('\n');
	}

	private static void appendValues(StringBuilder builder, InputStream is)
			throws InPUTException {
		appendLine(builder, is, 1);
	}

	private static void appendHeadline(StringBuilder builder, InputStream is)
			throws InPUTException {
		appendLine(builder, is, 0);
	}

	private static void appendLine(StringBuilder builder, InputStream is,
			int line) throws InPUTException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			for (int i = 0; i < line; i++)
				reader.readLine();
			builder.append(reader.readLine());
		} catch (IOException e) {
			throw new InPUTException(
					"The design file you process is malformed.", e);
		}
	}

	public static void appendDesigns(RVector designLines, StringBuilder b) {
		List<String[]> values = initValues(designLines);

		double[] repeats = extractRepeats(designLines);

		StringBuilder desBuilder;
		for (int i = 0; i < values.get(0).length; i++) {
			desBuilder = new StringBuilder();
			for (int j = 0; j < values.size(); j++) {
				desBuilder.append(values.get(j)[i]);
				desBuilder.append(' ');
			}
			desBuilder.append('\n');

			for (int j = 0; j < (int) repeats[i]; j++)
				b.append(desBuilder);
		}
	}

	private static double[] extractRepeats(RVector designLines) {
		int repeatPosition = designLines.size() - 3;
		return ((REXP) designLines.get(repeatPosition)).asDoubleArray();
	}

	private static List<String[]> initValues(RVector designLines) {
		List<String[]> params = new ArrayList<String[]>();
		String cache;
		for (int i = 0; i < designLines.size(); i++) {
			cache = designLines.get(i).toString().split("\\(")[1].split("\\)")[0];
			params.add(cache.split(", "));
		}
		return params;
	}

	public InputStream toInputStream() {
		return designStream.next();
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (SpotDesign design : designs) {
			b.append(design);
			b.append('\n');
		}
		return b.toString();
	}
}
