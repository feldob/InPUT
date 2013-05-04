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
 *//*-- $Copyright (C) 2012 Felix Dobslaw$

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

package se.miun.itm.input.model.design;

import java.util.Map;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.aspects.Importable;
import se.miun.itm.input.aspects.ParamSupportable;
import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;

/**
 * A design space is a range descriptor for the interface of an algorithm. It
 * consists of a set of parameters, potentially with a hierarchical structure.
 * Two types of parameters exist, structural and numeric parameters. While
 * numeric parameters have concrete value ranges that can be narrowed by the
 * user given extreme values, do structural parameters allow for multiple
 * choices of a parameter, potentially complex ones. Structural parameters can
 * contain further structural and numerical parametesr, in a hierarchical order.
 * 
 * Design spaces Allow the integration of algorithm designs and their spaces for
 * the purpose of source code simplification, input validation, algorithm design
 * creation, and documentation improvement, amongst others.
 * 
 * Design spaces can be imported and exported to different formats (e.g. xml,
 * zip, LaTeX). They allow for the creation of valud random or empty designs.
 * 
 * @author Felix Dobslaw
 * 
 */
public interface IDesignSpace extends Identifiable, Exportable,
		Importable<IDesign, Document>, ParamSupportable {

	/**
	 * Reads and validates a design file with respect to this design space.
	 * 
	 * @param expId
	 *            A user defined identifier for the returned design
	 * @param importer
	 *            An importing tool for different formats. Possibly created by a user.
	 * @return the imported design
	 * @throws InPUTException
	 */
	@Override
	IDesign impOrt(InPUTImporter<Document> importer) throws InPUTException;

	/**
	 * Returns an empty design for this design space.
	 * 
	 * @param expId
	 *            A user defined identifier for the returned design
	 * @return the empty design
	 * @throws InPUTException 
	 */
	IDesign nextEmptyDesign(String expId) throws InPUTException;

	/**
	 * Returns a completely initialized valid random design with respect to this
	 * design space.
	 * 
	 * @param expId
	 *            A user defined identifier for the returned design
	 * @return the generated random design
	 * @throws InPUTException
	 */
	IDesign nextDesign(String expId) throws InPUTException;

	/**
	 * Returns a completely initialized , read only, valid random design with
	 * respect to this design space. nextDesign(expId, false) behaves like
	 * nextDesign(expId).
	 * 
	 * @param expId
	 *            A user defined identifier for the returned design
	 * @return the generated random design
	 * @throws InPUTException
	 */
	IDesign nextDesign(String expId, boolean readOnly) throws InPUTException;

	/**
	 * Returns a randomly chosen parameter value, with respect to this valid
	 * algorithm design space.
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @return The randomly chosen parameter value
	 * @throws InPUTException
	 */
	<T> T next(String paramId) throws InPUTException;

	/**
	 * Returns a matrix of randomly chosen parameter values, with respect to
	 * this design space.
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param dimensions
	 *            specifies the amount of dimensions for the output with its
	 *            amount of desired entries
	 * @return The array of randomly chosen parameter value
	 * @throws InPUTException
	 */
	<T> T next(String paramId, int[] dimensions) throws InPUTException;

	/**
	 * Returns a randomly chosen parameter value, with respect to this valid
	 * algorithm design space. Only required if relative extreme values are
	 * defined, otherwise use next(String).
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param vars
	 *            The map of parameter iD pairs that have to be evaluated for
	 *            the extreme values in paramId.
	 * @return The randomly chosen parameter value
	 * @throws InPUTException
	 */
	<T> T next(String paramId, Map<String, Object> vars) throws InPUTException;

	/**
	 * Returns a matrix of randomly chosen parameter values, with respect to
	 * this design space. Only required if relative extreme values are defined,
	 * otherwise use next(String, int[]).
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param dimensions
	 *            specifies the amount of dimensions for the output with its
	 *            amount of desired entries
	 * @param vars
	 *            The map of parameter Id pairs that have to be evaluated for
	 *            the extreme values in paramId.
	 * @return The array of randomly chosen parameter value
	 * @throws InPUTException
	 */
	<T> T next(String paramId, int[] dimensions, Map<String, Object> vars)
			throws InPUTException;

	/**
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param objects
	 *            user chosen objects that should be used for the constructor call of the
	 *            random choice object for type paramId.
	 * @return A random choice of the parameter with id paramId
	 * @throws InPUTException
	 */
	<T> T next(String paramId, Object[] objects) throws InPUTException;

	/**
	 * 
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param dimensions
	 *            specifies the amount of dimensions for the output with its
	 *            amount of desired entries
	 * @param objects
	 *            user chosen objects that should be used for the constructor call of the
	 *            random choice object for type paramId.
	 * @return A random choice of the parameter with id paramId
	 * @throws InPUTException
	 */
	<T> T next(String paramId, int[] dimensions, Object[] objects)
			throws InPUTException;

	/**
	 * Returns a randomly chosen parameter value, with respect to this valid
	 * algorithm design space. Only required if relative extreme values are
	 * defined, otherwise use next(String).
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param vars
	 *            The map of parameter iD pairs that have to be evaluated for
	 *            the extreme values in paramId.
	 * @param objects
	 *            user chosen objects that should be used for the constructor call of the
	 *            random choice object for type paramId.* @param objects
	 *            user chosen objects that should be used for the constructor call of the
	 *            random choice object for type paramId.
	 * @return The randomly chosen parameter value
	 * @throws InPUTException
	 */
	<T> T next(String paramId, Map<String, Object> vars, Object[] objects)
			throws InPUTException;

	/**
	 * Returns a randomly chosen parameter value, with respect to this valid
	 * algorithm design space. Only required if relative extreme values are
	 * defined, otherwise use next(String).
	 * 
	 * @param paramId
	 *            The parameter id for a valid parameter type definition in this
	 *            design space.
	 * @param dimensions
	 *            specifies the amount of dimensions for the output with its
	 *            amount of desired entries
	 * @param vars
	 *            The map of parameter iD pairs that have to be evaluated for
	 *            the extreme values in paramId.
	 * @param objects
	 *            user chosen objects that should be used for the constructor call of the
	 *            random choice object for type paramId.* @param objects
	 *            user chosen objects that should be used for the constructor call of the
	 *            random choice object for type paramId.
	 * @return The randomly chosen parameter value
	 * @throws InPUTException
	 */
	<T> T next(String paramId, int[] dimensions, Map<String, Object> vars,
			Object[] objects) throws InPUTException;

	/**
	 * Checks if the given design space is backed up or imported from a file.
	 * @return
	 */
	boolean isFile();

	/**
	 * returns the file name of the design space file if present, otherwise null.
	 * @return
	 */
	String getFileName();

	/**
	 * sets a parameter decision as fixed to the given value. Can be revoked by setting null.
	 * @param string
	 * @param caSe
	 */
	void setFixed(String paramId, String valueString) throws InPUTException;
}