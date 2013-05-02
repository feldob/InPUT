package se.miun.itm.input.model.param.generator;

import java.util.Map;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;

public interface IValueGenerator {

	Object parse(String valueString) throws InPUTException;

	Object next(int[] dimensions, Map<String, Object> vars)
			throws InPUTException;

	Object next(Map<String, Object> vars) throws InPUTException;

	Object invokeGetter(Object value) throws InPUTException;

	void invokeSetter(Object parentValue, Object value)
			throws InPUTException;

	boolean hasGetHandle();

	boolean hasSetHandle();

	void validateInPUT(String paramId, Object value, ElementCache elementCache) throws InPUTException;
	
	boolean initByConstructor(String paramId);
	
}