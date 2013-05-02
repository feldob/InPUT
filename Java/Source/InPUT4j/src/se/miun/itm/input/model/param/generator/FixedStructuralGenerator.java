package se.miun.itm.input.model.param.generator;

import java.util.Map;
import java.util.regex.Pattern;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.model.param.SParam;
import se.miun.itm.input.util.Q;

/**
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class FixedStructuralGenerator extends StructuralGenerator {

	private String fixedValue;

	private final String[] fixedIds;

	public FixedStructuralGenerator(AStruct param, String fixedValue)
			throws InPUTException {
		super(param, null);
		this.fixedValue = fixedValue;
		fixedIds = fixedValue.split(Pattern.quote(" "));
		validateFixed();
	}

	private void validateFixed() throws InPUTException {
		if (isComplex()) {
			int dimensions = param.getDimensions()[0];
			if (fixedIds.length > dimensions)
				throw new InPUTException(
						"The fixed value setup for complex type \""
								+ param.getId()
								+ "\" is inconsistent: Too many entries, only "
								+ dimensions + " allowed.");
		}
	}

	@Override
	public Object handleComplex(Map<String, Object> vars,
			Object[] actualParams, Object[] array) throws InPUTException {
		Object value;
		
		if (isComplex())
		{
			adjustToFixed(array);
			value = makeComplex(array, vars, actualParams);
		}
		else
			value = array;
		return value;
	}
	
	private void adjustToFixed(Object[] array) throws InPUTException {
		for (int i = 0; i < fixedIds.length; i++) {
			if (!fixedIds[i].equals(Q.PLACEHOLDER)) {
				array[i] = param.getValueForString(fixedIds[i]);
			}
		}
	}

	@Override
	public Object next(Map<String, Object> vars) throws InPUTException {
		if (isComplex())
			return ((SParam) param).nextChoice();
		 return getChoice();
	}

	@Override
	public Object parse(String valueString) throws InPUTException {
		if (isComplex())
			return ((SParam) param).getChoiceById(valueString).newInstance(null);
		return getChoice().getValueForString(valueString);
	}

	public AStruct getChoice() {
		if (param instanceof SChoice) {
			return param;
		} else
			return ((SParam) param).getChoiceById(fixedValue);
	}

	@Override
	public String getValueForIndex(int index) throws InPUTException {
		if (isComplex())
			return ((SParam) param).getChoiceByPosition(index).getLocalId();
		return getChoice().getLocalId();
	}

	@Override
	public void validateInPUT(String paramId, Object value, ElementCache elementCache)
			throws InPUTException {
		if (isComplex()) {
			validateComplex(value);
		} else {
			super.validateInPUT(paramId, value, elementCache);
			if (isEnum()) {
				validateEnum(value);
			} else if (!getChoice().getSuperClass().isInstance(value))
				throw new InPUTException("The object \"" + value.toString()
						+ "\" is of the wrong type. \""
						+ getChoice().getSuperClass().getName()
						+ "\" was expected, but was "
						+ value.getClass().getName() + ".");
		}
	}

	private void validateEnum(Object value) throws InPUTException {
		Object thisValue = Enum.valueOf(getEnumValue().getDeclaringClass(),
				fixedValue);
		if (!thisValue.equals(value))
			throw new InPUTException("The enum \"" + value.toString()
					+ "\" is of the wrong type. \"" + fixedValue
					+ "\" was expected, but was " + value.toString() + ".");
	}

	private void validateComplex(Object value) throws InPUTException {
		if (!getComplex().getType().isInstance(value))
			throw new InPUTException(
					param.getId()
							+ ": a complex value is expected to be of complex type.");

		//TODO add some validity check for the amount of entries (requires the reflection of get part)
//		int dimensions = param.getDimensions()[0];
	}
}