package se.miun.itm.input.tuning.sequential.spot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.export.SpotExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.InputStreamWrapper;

public class SpotROI implements SpotExportable<InputStream>{

	private static final SpotExporter exporter = new SpotExporter();

	private final InputStreamWrapper roi;

	private final List<SpotParam> params;
	
	private final String functionId;

	private SpotROI(Object helper, String functionId) throws InPUTException {
		this.functionId = functionId;
		roi = initRoi(helper);
		params = initTable(roi);
	}

	public SpotROI(IDesignSpace space) throws InPUTException {
		this(space, space.getId());
	}

	public SpotROI(InputStream roi, String functionId) throws InPUTException,
			FileNotFoundException {
		this((Object)roi, functionId);
	}

	public SpotROI(String path, long configId, String functionId) throws InPUTException,
			FileNotFoundException {
		this(new FileInputStream(path), functionId);
	}

	public SpotROI(IInPUT input) throws InPUTException {
		this(input, input.getAlgorithmDesignSpace().getId());
	}

	private InputStreamWrapper initRoi(Object helper) throws InPUTException {
		InputStreamWrapper roi = null;
		try {
			if (helper instanceof IDesignSpace) {
				roi = new InputStreamWrapper(
						((IDesignSpace) helper).export(exporter));
			} else if (helper instanceof IInPUT){
				roi = new InputStreamWrapper(
						((IInPUT) helper).export(exporter));
			}
			else if (helper instanceof InputStream) {
				roi = new InputStreamWrapper((InputStream) helper);
			} 
		} catch (IOException e) {
			throw new InPUTException("The roi file could not be created.", e);
		}
		return roi;
	}

	public List<SpotParam> getParams() {
		return params;
	}

	private static List<SpotParam> initTable(InputStreamWrapper roi) throws InPUTException {
		List<SpotParam> params = new ArrayList<SpotParam>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				roi.next()));
		try {
			reader.readLine();
			while (reader.ready())
				params.add(new SpotParam(reader.readLine()));
		} catch (IOException e) {
			throw new InPUTException("The roi stream contains an error.", e);
		}
		return Collections.unmodifiableList(params);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotROI))
			return false;

		List<SpotParam> foreignParams = ((SpotROI) obj).getParams();
		
		if (foreignParams.size() != params.size())
			return false;
			
		if (foreignParams.size() == params.size())
			for (int i = 0; i < params.size(); i++)
				if (!params.get(i).equals(foreignParams.get(i)))
					return false;
		return true;
	}

	public String getFunctionId() {
		return functionId;
	}

	@Override
	public InputStream export() {
		return roi.next();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("ROI for ");
		b.append(functionId);
		b.append(":\n");
		for (SpotParam param : params) {
			b.append(param.toString());
			b.append('\n');
		}
		return b.toString();
	}
}
