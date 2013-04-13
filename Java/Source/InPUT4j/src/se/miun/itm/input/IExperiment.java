/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */
package se.miun.itm.input;

import java.util.Map;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Extendable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.aspects.Importable;
import se.miun.itm.input.aspects.Outable;
import se.miun.itm.input.aspects.Valuable;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.util.InputStreamWrapper;

/**
 * An experiment summarizes a computer experiment, and is always defined with
 * respect to a InPUT meta design space object. It contains optional designs for
 * a choice of algorithm, problem, and preferences that depend on both,
 * algorithm and problems. In addition, it allows for the adding and removing of
 * outputs that are defined with respect to the output space of the InPUT
 * instance.
 * 
 * Experiments serve for the purpose of documentation, and automatic
 * instantiation of whole series of computer experiments. They can be exported
 * to zip files or database structures, serving for documentation and an easy
 * exchange between different users, on different systems.
 * 
 * Each experiment allows for the getting and setting of values for each input design
 * that it contains (contains them all in its scope). Therefore, it requires that all designs, used in its scope, have different parameter ids.
 * 
 * @author Felix Dobslaw
 * 
 */
public interface IExperiment extends Identifiable, Exportable, Extendable,
		Valuable, Outable, Importable<Void, Map<String, InputStreamWrapper>> {

	/**
	 * Returns the preferences design of this experiment.
	 * 
	 * @return
	 */
	IDesign getPreferences();

	/**
	 * Resets the preferences design for this experiment.
	 * 
	 * @param properties
	 */
	void setPreferences(IDesign properties);

	/**
	 * Returns the algorithm design for this experiment.
	 * 
	 * @return
	 */
	IDesign getAlgorithmDesign();

	void setAlgorithmDesign(IDesign algorithm);

	/**
	 * Returns the problem features, that make up for the problem instance of
	 * this experiment..
	 * 
	 * @return
	 */
	IDesign getProblemFeatures();

	/**
	 * Resets the problem features for this experiment.
	 * 
	 * @param features
	 */
	void setProblemFeatures(IDesign features);

	/**
	 * imports the problem features from a design file that is expected to be in
	 * the file system, following the Design.xsl structure.
	 * 
	 * @param featuresPath
	 * @throws InPUTException
	 */
	void importProblemFeatures(String featuresPath) throws InPUTException;

	/**
	 * imports the algorithm choices from a design file that is expected to be
	 * in the file system, following the Design.xsl structure.
	 * 
	 * @param featuresPath
	 * @throws InPUTException
	 */
	void importAlgorithmDesign(String algorithmPath) throws InPUTException;

	/**
	 * imports preferences from a design file that is expected to be in the file
	 * system, following the Design.xsl structure.
	 * 
	 * @param featuresPath
	 * @throws InPUTException
	 */
	void importProperties(String propertiesPath) throws InPUTException;

	/**
	 * Returns the InPUT meta instance for this computer experiment.
	 * 
	 * @return
	 */
	IInPUT getInPUT();

	boolean investigatesSameConfiguration(IExperiment experiment);

	boolean same(IExperiment algorithmDesign);
}