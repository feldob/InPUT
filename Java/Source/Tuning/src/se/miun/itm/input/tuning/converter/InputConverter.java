package se.miun.itm.input.tuning.converter;

import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesignSpace;

public interface InputConverter<ExternalDesign, ExternalSpace, ExternalResult> {

	ExternalDesign fromExperiments(List<IExperiment> inputDesign) throws InPUTException;

	ExternalSpace fromDesignSpace(IDesignSpace inputSpace) throws InPUTException;

	IExperiment toExperiment(String spaceId, ExternalDesign design, int position) throws InPUTException;
	
	List<IExperiment> toExperiments(String spaceId, ExternalDesign design) throws InPUTException;

	IDesignSpace toDesignSpace(ExternalSpace designSpace) throws InPUTException;

	ExternalResult fromResults(List<IExperiment> results) throws InPUTException;
}