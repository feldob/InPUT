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
 */package se.miun.itm.input.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Pattern;

import org.jdom2.Element;

import se.miun.itm.input.model.DimensionHelper;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.element.Value;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.generator.ValueGenerator;

/**
 * A class upplying basic features to support the extraction of information from
 * param elements or parsing and processing of id related services.
 * 
 * @author Felix Dobslaw
 * 
 * @ThreadSafe
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
	 * returns the parameter from the parameter store, given its id.
	 * 
	 * @param paramId
	 * @param parentParam
	 * @param ps
	 * @return
	 */
	public static Param<?> getParamForId(String paramId, Param<?> parentParam,
			ParamStore ps) {
		Param<?> param = ps.getParam(paramId);
		if (param == null)
			param = getParamForLocalId(paramId, parentParam, ps);
		
		//TODO this is strongly heuristic, and there are cases where this might not work well. A quick fix before restructuring of InPUT.
		if (param == null)
			param = ps.getParamForAnyStore(paramId);
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
	public static Param<?> getParamForLocalId(String paramId,
			Param<?> parentParam, ParamStore ps) {
		Param<?> localParam = ps.getParam(parentParam.getParamId() + "."
				+ paramId);
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
					ValueGenerator.EMPTY_CLASS_ARRAY);
			return method
					.invoke(parentValue, ValueGenerator.EMPTY_OBJECT_ARRAY);
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
		Param<?> parentParam = ps.getParam(paramId);
		Param<?> param = getParamForLocalId(valueParamId, parentParam, ps);
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
		BufferedReader r = null;
		try {
			String temp;
			r = new BufferedReader(new FileReader(new File(
					descriptorFile).getAbsolutePath()));
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
		}finally{
			try {
				r.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	 * the suitable id for the parameter depending on the value type (array,
	 * simple, collection).
	 * 
	 * @param original
	 * @param param
	 * @return
	 */
	public static String deriveValueReferenceId(final Element original,
			final Param<?> param) {
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
	 * 
	 * @param id
	 * @return
	 */
	public static String deriveLocalId(String id) {
		String[] chops = id.split(Pattern.quote("."));
		return chops[chops.length - 1];
	}

	/**
	 * returns the smallest id start that both ids have in common, potentially
	 * "".
	 * 
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
				b.append('.');
				i++;
			} else
				break;
		}

		b.append(chops2[i]);
		b.append('.');
		return b.toString();
	}

	/**
	 * retrieve the parameter id for a given InPUT parameter.
	 * 
	 * @param param
	 * @return
	 */
	public static String deriveParamId(Element param) {
		return deriveParamId(param, param.getAttributeValue(Q.ID_ATTR));
	}

	public static String deriveInputValueId(Element value) {
		Element parent = value.getParentElement();
		String localId = value.getAttributeValue(Q.ID_ATTR);
		if (value.getName().equals(Q.SVALUE))
			localId = value.getAttributeValue(Q.ID_ATTR) + "."
					+ value.getAttributeValue(Q.VALUE_ATTR);
		return deriveInputValueId(parent, localId);
	}

	public static String deriveInputValueId(Element value, String localId) {
		if (value == null || value.isRootElement())
			return localId;
		return deriveInputValueId(value.getParentElement()) + "." + localId;
	}

	/**
	 * derive parameter id for a given InPUT element.
	 * 
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
	 * 
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

	public static Object repackArrayForImport(Object value) {
		if (value.getClass().isArray()) {
			Object[] internal = new Object[Array.getLength(value)];
			for (int j = 0; j < internal.length; j++) {
				internal[j] = repackArrayForImport(Array.get(value, j));
			}
			value = internal;
		}

		return value;
	}

	public static Object packArrayForExport(Class<?> cLass, Object value,
			int dimensions) {
		if (value.getClass().isArray() && dimensions > 0) {
			Object subExport;
			Object export = null;
			for (int i = 0; i < Array.getLength(value); i++) {
				subExport = packArrayForExport(cLass, Array.get(value, i),
						dimensions - 1);
				if (i == 0) {
					Class<?> choice = subExport.getClass();
					if (dimensions == 1) {
						choice = cLass;
					}
					export = Array.newInstance(choice, Array.getLength(value));
				}
				Array.set(export, i, subExport);
			}
			value = export;
		}

		return value;
	}

	public static String deriveInputParamId(ParamStore store, Element element) {
		if (element.isRootElement())
			return "";

		Stack<String> shoppedIdStack = createStack(element);

		String id = shoppedIdStack.pop();
		String value, nextId;
		while (!shoppedIdStack.isEmpty()) {
			value = shoppedIdStack.pop();
			nextId = shoppedIdStack.pop();
			if (value == null) {
				id += "." + nextId;
			} else if (store.containsParam(id + "." + value)) { // the param exists, value is not an index
				if (store.containsParam(id + "." + value +"."+ nextId)) { // if this exists, we know that the param is a child of the next id entry.
					id += "." + value + "." + nextId;
				} else {
					id += "." + nextId;
				}
			}
		}

		return id;
	}

	private static Stack<String> createStack(Element element) {
		Stack<String> stack = new Stack<String>();
		stack.push(element.getAttributeValue(Q.ID_ATTR));
		Element current = element.getParentElement();
		while (!current.isRootElement()) {
			stack.push(current.getAttributeValue(Q.VALUE_ATTR));
			stack.push(current.getAttributeValue(Q.ID_ATTR));
			current = current.getParentElement();
		}
		return stack;
	}

	public static boolean isIntegerString(String stringValue) {
		try {
			Integer.parseInt(stringValue);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean isDimensionalArray(int[] sizeArray) {
		return !(sizeArray.length == 0 || (sizeArray.length == 1 && sizeArray[0] == 0));
	}

	public static int[] initDimensions(int[] sizeArray) {
		if (Param.isArrayType(sizeArray))
			return sizeArray;
		else
			return DimensionHelper.DEFAULT_DIM;
	}

	public static Param<?> retrieveParamForValueE(AStruct param,
			Element originalChild, Element originalChoice)
			throws InPUTException {
		// get the param for sub value. A sub value can be of two types
		String choiceLocalId = originalChoice.getAttributeValue(Q.VALUE_ATTR);
		String subParamId = originalChild.getAttributeValue(Q.ID_ATTR);
		// this is a subentry of an array type, get back to the parent type.
		if (choiceLocalId == null && ParamUtil.isIntegerString(subParamId))
			return null;
		else {
			// either its child of the choice or the param.
			Param<?> subParam = param.getChildParamElement(subParamId);
			if (subParam == null) {
				// chances are its a neighbor param of the parent. lets get it
				// via lookup!
				subParam = param.getChildParamElement(subParamId);
				if (subParam == null) {
					AStruct choice = param.getChoiceById(choiceLocalId);
					if (choice == null) {
						throw new InPUTException(
								"Configuration error for parameter \""
										+ param.getId()
										+ "\". Potential sources: "
										+ "1) You set a sub-parameter \""
										+ subParamId
										+ "\" to the configuration, which does not exist. 2) "
										+ "you misspelled the choice entry in your design: \""
										+ choiceLocalId
										+ "\". In any case, make sure that the identfiers in design (value), design space and mapping (choice or parameter) match.");
					}
					subParam = choice.getChildParamElement(subParamId);
				}
			}
			return subParam;
		}
	}

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	public static String getParentId(Param<?> param) {
		Element parent = param.getParentElement();
		if (parent.isRootElement())
			return "";

		if (parent instanceof SChoice)
			return ((Param<?>) parent.getParentElement()).getId();
		return ((Param<?>) parent).getId();
	}
}