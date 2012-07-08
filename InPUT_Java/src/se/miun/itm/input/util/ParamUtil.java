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
package se.miun.itm.input.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.jdom.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;

/**
 * A class upplying basic features to support the extraction of information from
 * param elements or parsing and processing of id related services.
 * 
 * @author Felix Dobslaw
 * 
 */
public class ParamUtil {

	/**
	 * Returns yes if the identifier of a constructor parameter, passed in the
	 * code mapping file, is of method type.
	 * 
	 * @param identifier
	 * @return
	 */
	public static boolean isMethodContext(String identifier) {
		return identifier.contains("(");
	}

	/**
	 * Retrieves the return type class for a given a method identifier and a
	 * param element.
	 * 
	 * @param identifier
	 * @param parentParam
	 * @param ps
	 * @return
	 * @throws InPUTException
	 */
	public static Class<?> getClassFromMethodReturnType(String identifier,
			Param parentParam, ParamStore ps) throws InPUTException {
		String[] chops = identifier.split(Pattern.quote("."));
		String methodId = chops[chops.length - 1];

		String paramId = identifier.substring(0, identifier.length()
				- (methodId.length() + 1));

		Param param = getParamForId(paramId, parentParam, ps);

		Class<?> cLass = param.getInPUTClass();

		try {
			return cLass.getMethod(methodId, AStruct.EMPTY_CLASS_ARRAY)
					.getReturnType();
		} catch (Exception e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	/**
	 * returns the parameter from the parameter store, given its id.
	 * 
	 * @param paramId
	 * @param parentParam
	 * @param ps
	 * @return
	 */
	public static Param getParamForId(String paramId, Param parentParam,
			ParamStore ps) {
		Param param = ps.getParam(paramId);
		if (param == null)
			param = getParamForLocalId(paramId, parentParam, ps);
		return param;
	}

	/**
	 * returns the local id of a parameter, given its global id.
	 * 
	 * @param paramId
	 * @param parentParam
	 * @param ps
	 * @return
	 */
	public static Param getParamForLocalId(String paramId, Param parentParam,
			ParamStore ps) {
		Param localParam = ps
				.getParam(parentParam.getParamId() + "." + paramId);
		if (localParam == null)
			localParam = ps.getParam(parentParam.getId() + "." + paramId);
		return localParam;
	}

	/**
	 * invokes the method with identifier identifier for parameter param, given
	 * the context that the cache provides.
	 * 
	 * @param identifier
	 * @param paramElementId
	 * @param paramId
	 * @param ps
	 * @param cache
	 * @return
	 * @throws InPUTException
	 */
	public static Object getMethodContextValue(String identifier,
			String paramElementId, String paramId, ParamStore ps,
			ElementCache cache) throws InPUTException {

		String[] chops = identifier.split(Pattern.quote("."));
		String methodId = chops[chops.length - 1];
		methodId = methodId.substring(0, methodId.length() - 2);

		String valueParamId = identifier.substring(0, identifier.length()
				- (methodId.length() + 3));

		Value<?> value = getValueForId(valueParamId, paramElementId, paramId,
				ps, cache);

		Object parentValue = value.getInputValue(null);

		try {
			Method method = parentValue.getClass().getMethod(methodId,
					AStruct.EMPTY_CLASS_ARRAY);
			return method.invoke(parentValue, AStruct.EMPTY_OBJECT_ARRAY);
		} catch (Exception e) {
			throw new InPUTException(e.getMessage(), e);
		}
	}

	/**
	 * returns the InPUT value element for a given param id, and its parent id,
	 * given the element cache as a context.
	 * 
	 * @param valueParamId
	 * @param paramElementId
	 * @param paramId
	 * @param ps
	 * @param cache
	 * @return
	 */
	private static Value<?> getValueForId(String valueParamId,
			String paramElementId, String paramId, ParamStore ps,
			ElementCache cache) {
		// first lookup for local param.
		Param parentParam = ps.getParam(paramId);
		Param param = getParamForLocalId(valueParamId, parentParam, ps);
		// no success -> lookup in cache directly by global name.
		String eventualParamId;

		if (param != null) {
			eventualParamId = param.getId();
		} else {
			eventualParamId = valueParamId;
		}
		return cache.get(eventualParamId);
	}

	/**
	 * Is the parameter potentially a global parameter or an array object?
	 * 
	 * @param identifier
	 * @return
	 */
	public static boolean hasGlobalParamSyntax(String identifier) {
		return identifier.contains(".") || identifier.contains("[");
	}

	/**
	 * extracts the descriptor id for a given descriptor file without using an
	 * xml parser.
	 * 
	 * @param descriptorFile
	 * @return
	 * @throws InPUTException
	 */
	public static String extractDescriptorId(String descriptorFile)
			throws InPUTException {
		try {
			String temp;
			BufferedReader r = new BufferedReader(
					new FileReader(descriptorFile));
			while (r.ready()) {
				temp = r.readLine();
				if (temp.contains("id=\""))
					return ParamUtil.extractId(temp);
			}

		} catch (FileNotFoundException e) {
			throw new InPUTException(
					"The descriptor id could not be extracted from '"
							+ descriptorFile + "'. Is the attribute missing?",
					e);
		} catch (IOException e) {
			throw new InPUTException(
					"The descriptor id could not be extracted from '"
							+ descriptorFile + "'. Is it missing?", e);
		}
		return null;
	}

	/**
	 * a helper function that retrieves the value of the id field from a string.
	 * 
	 * @param temp
	 * @return
	 */
	public static String extractId(String temp) {
		String[] chops = temp.split(Pattern.quote("\""));
		for (int i = 0; i < chops.length; i++)
			if (chops[i].contains("id="))
				return chops[i + 1];
		return null;
	}

	/**
	 * for a given original xml element and an InPUT parameter element, retrieve
	 * the suitable id for the parameter depending on the value type (array, simple, collection).
	 * 
	 * @param original
	 * @param param
	 * @return
	 */
	public static String deriveValueReferenceId(final Element original,
			final Param param) {
		// if original contains an integer as id : its a reference to a matrix
		// parameter and the id should be given containing the int entries.

		if (original == null
				|| param.getLocalId().equals(
						original.getAttributeValue(Q.ID_ATTR)))
			return deriveParamId(param);
		else
			return deriveParamId(original);
	}

	/**
	 * retrieve the local id for a give parameter id.
	 * @param id
	 * @return
	 */
	public static String deriveLocalId(String id) {
		String[] chops = id.split(Pattern.quote("."));
		return chops[chops.length - 1];
	}

	/**
	 * returns the smallest id start that both ids have in common, potentially "".
	 * @param dependantId
	 * @param dependeeId
	 * @return
	 */
	public static String deriveLowestNonSharedId(String dependantId,
			String dependeeId) {
		String[] chops1 = dependantId.split(Pattern.quote("."));
		String[] chops2 = dependeeId.split(Pattern.quote("."));
		StringBuilder b = new StringBuilder();

		int i = 0;
		while (chops1.length > i && chops2.length > i) {
			if (chops2[i].equals(chops1[i])) {
				b.append(chops2[i]);
				b.append(".");
				i++;
			} else
				break;
		}

		b.append(chops2[i]);
		b.append(".");
		return b.toString();
	}

	/**
	 * retrieve the parameter id for a given InPUT parameter.
	 * @param param
	 * @return
	 */
	public static String deriveParamId(Element param) {
		return deriveParamId(param, param.getAttributeValue(Q.ID_ATTR));
	}

	/**
	 * derive parameter id for a given InPUT element.
	 * @param element
	 * @param name
	 * @return
	 */
	private static String deriveParamId(final Element element, String name) {
		// 1) not null, 2) is Element, 3) not root -> next!
		if (element.getParent() != null
				&& element.getParent() instanceof Element) {
			Element parent = (Element) element.getParent();
			if (!parent.isRootElement())
				name = deriveParamId(parent,
						parent.getAttributeValue(Q.ID_ATTR) + "." + name);
		}

		return name;
	}

	/**
	 * extract the id for a given codemapping stream.
	 * @param codeMappingStream
	 * @return
	 * @throws InPUTException
	 */
	public static String extractDescriptorId(InputStream codeMappingStream)
			throws InPUTException {
		try {
			String temp;
			BufferedReader r = new BufferedReader(new InputStreamReader(
					codeMappingStream));
			while (r.ready()) {
				temp = r.readLine();
				if (temp.contains("id=\""))
					return ParamUtil.extractId(temp);
			}

		} catch (FileNotFoundException e) {
			throw new InPUTException(
					"The descriptor id could not be extracted. Is the attribute missing?",
					e);
		} catch (IOException e) {
			throw new InPUTException(
					"The descriptor id could not be extracted. Is it missing?",
					e);
		}
		return null;
	}
}