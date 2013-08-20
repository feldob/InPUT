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
 */package se.miun.itm.input;

import java.io.IOException;
import java.util.Map;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.InputStreamWrapper;

/**
 * The InPUT object is a descriptor for whole experimental investigations. It
 * allows for the description of all scopes that are relevant for the
 * investigation, including the algorithm, the problem, and the properties that
 * relate to both, algorithm and problem, Each part can optionally be defined by
 * a design space. In extension, each InPUT instance has an optional output
 * design space, which describes the expected results for the study.
 * 
 * InPUT instances can be imported and exported using the zip and xml
 * importer/exporter in InPUT4j. Whole experiments can be created at random,
 * given a valid InPUT description. The relation between InPUT and Experiment is
 * similar to the relation between Design Space and Design.
 * 
 * @author Felix Dobslaw
 * 
 */
public interface IInPUT extends Identifiable, Exportable {

	/**
	 * returns the problem feature space.
	 * 
	 * @return
	 */
	IDesignSpace getProblemFeatureSpace();

	/**
	 * returns the algorithm design space.
	 * 
	 * @return
	 */
	IDesignSpace getAlgorithmDesignSpace();

	/**
	 * returns the property space.
	 * 
	 * @return
	 */
	IDesignSpace getPropertySpace();

	/**
	 * returns the output space for the InPUT instance.
	 * 
	 * @return
	 */
	IDesignSpace getOutputSpace();

	/**
	 * imports an experiment from an external source, which is an instance of
	 * this InPUT meta space.
	 * 
	 * @param id
	 * @param importer
	 * @return
	 * @throws InPUTException
	 * @throws IOException 
	 */
	IExperiment impOrt(String id, InPUTImporter<Map<String, InputStreamWrapper>> importer)
			throws InPUTException, IOException;

	IExperiment nextExperiment(String expId, IDesign problemFeatures)
			throws InPUTException;
}