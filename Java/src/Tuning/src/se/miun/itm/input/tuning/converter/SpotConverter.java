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
 */package se.miun.itm.input.tuning.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.tuning.sequential.spot.SpotDES;
import se.miun.itm.input.tuning.sequential.spot.SpotDesign;
import se.miun.itm.input.tuning.sequential.spot.SpotRES;
import se.miun.itm.input.tuning.sequential.spot.SpotROI;

public class SpotConverter implements InputConverter<SpotDES, SpotROI, SpotRES> {

	public static final String ID_CHOP_PATTERN = Pattern.quote(".");

	private final SpotDesignInitializer experimentInitializer;

	private AtomicInteger designId = new AtomicInteger();

	public SpotConverter() throws InPUTException{
		 experimentInitializer = new SpotDesignInitializer();
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
	public SpotROI fromDesignSpace(IDesignSpace inputSpace) throws InPUTException {
		if (inputSpace == null)
			throw new InPUTException("No space given.");

		return new SpotROI(inputSpace);
	}

	@Override
	public List<IExperiment> toExperiments(String inputId, SpotDES experimentalDesign) throws InPUTException {
		if (experimentalDesign == null)
			throw new InPUTException("No design given.");

		IInPUT input = InPUT.lookup(inputId);

		try {
			return toExperiments(experimentalDesign, input);
		} catch (IOException e) {
			throw new InPUTException("Something went wrong while reading the SPOT design file.");
		}
	}

	@Override
	public IExperiment toExperiment(String inputId, SpotDES experimentalDesign, int position) throws InPUTException {
		if (experimentalDesign == null)
			throw new InPUTException("No design given.");

		IInPUT input = InPUT.lookup(inputId);

		try {
			return toExperiment(experimentalDesign.getDesign(position), input);
		} catch (IOException e) {
			throw new InPUTException("Something went wrong while reading the SPOT design file.");
		}
	}

	private IExperiment toExperiment(SpotDesign design, IInPUT input) throws IOException, InPUTException {
		List<Param<?>> fixed = ParamStore.getFixed(input);
		int id = designId.incrementAndGet();
		return experimentInitializer.initExperiment(id, fixed, design, input);
	}
	
	private List<IExperiment> toExperiments(SpotDES des, IInPUT input) throws IOException, InPUTException {

		List<Param<?>> fixed = ParamStore.getFixed(input);

		IExperiment experiment = null;
		List<IExperiment> experiments = new ArrayList<IExperiment>();
		for (SpotDesign spotDesign : des.getDesigns()) {
			int id = designId.incrementAndGet();
			experiment = experimentInitializer.initExperiment(id, fixed, spotDesign, input);
			experiments.add(experiment);
		}
		return experiments;
	}

	@Override
	public IDesignSpace toDesignSpace(SpotROI designSpace) throws InPUTException {
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