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
 */package se.miun.itm.input.model.design;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.aspects.Importable;
import se.miun.itm.input.aspects.ParamSupportable;
import se.miun.itm.input.aspects.Valuable;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;

/**
 * a design contains a set of parameter values, always defined with respect to a
 * valid design space (IDesignSpace). The IDesign interface allows for the
 * import of designs from external sources, such as XML, bytestreams or
 * databases. It allows for the retrieval and setting of values at runtime. The
 * parameter scope of a design can be extended by other designs. All designs
 * always have full read/write access to the InPUT configuration parameters.
 * 
 * In general, designs should be seen as instances, or implementations of design
 * spaces. They contain concrete values that are valid with respect to the
 * design space they relate to. Given the design space of a search algorithm,
 * which consists of all degrees of freedom (parameters) for a full
 * instantiation of the algorithm, a design is a set of choices for each of the
 * degrees.
 * 
 * Because a design space allows for hierarchical parameters of any depth, does
 * a design contain choices of the within the design space defined depth.
 * 
 * @author Felix Dobslaw
 * 
 */
public interface IDesign extends Identifiable, Exportable, Valuable, Importable<Void, Document>, ParamSupportable {

	/**
	 * Imports an algorithm design file. This will load a design tree into
	 * memory.
	 * 
	 * @param expPathName
	 *            The full path of the algorithm design file to be imported
	 * @throws InPUTException
	 */
	@Override
	Void impOrt(InPUTImporter<Document> importer) throws InPUTException;

	/**
	 * Subsequent calls to {@link #setValue(String, Object) setValue} will throw
	 * an InPUTExceptions.
	 */
	void setReadOnly();

	/**
	 * returns the design space which this design is an instance of.
	 * 
	 * @return
	 */
	IDesignSpace getSpace();

	/**
	 * Extends the design by system information, such as its architecture, the
	 * amount of cores, available memory etc.
	 */
	void attachEnvironmentInfo();

	/**
	 * Extends the scope for parameter access to all parameters of the neighbor
	 * design. Addressing is done absolute with respect to the parameter names,
	 * which has as a consequence that parameters in neighboring designs may not
	 * share the same id.
	 * 
	 * @param neighbor
	 */
	void extendScope(IDesign neighbor);

	/**
	 * compares if two designs are defining the same configuration; not
	 * necessarily having the same id, and are not necessarily equal.
	 * 
	 * @param obj
	 * @return
	 */
	boolean same(Object obj);

	IDesign toClone() throws InPUTException;

	void resetId(String id);
}