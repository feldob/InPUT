package se.miun.itm.input.model.param.generator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.element.ElementCache;
import se.miun.itm.input.model.mapping.IMapping;
import se.miun.itm.input.model.mapping.IMappings;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.model.mapping.NumericEmptyMapping;
import se.miun.itm.input.model.param.AStruct;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.util.Q;

/**
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public abstract class ValueGenerator<AMapping extends IMapping, AParam extends Param<?>> implements
		IValueGenerator {

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	protected final Random rng;

	protected AParam param;

	protected AMapping mapping;

	// @LazyLoading
	private Method getHandle;
	private Boolean hasGetHandle;

	// @LazyLoading
	private Method setHandle;
	private Boolean hasSetHandle;

	public ValueGenerator(AParam param, Random rng)
			throws InPUTException {
		this.rng = rng;
		this.param = param;
		this.mapping = initMappings();
	}

	@Override
	public Object next(int[] dimensions, Map<String, Object> vars)
			throws InPUTException {
		Object value;
		if (dimensions.length > 1)
			value = nextArray(dimensions, vars);
		else
			value = nextValue(dimensions[0], vars);
		return value;
	}
	
	protected Object nextValue(Integer type, Map<String, Object> vars)
			throws InPUTException {
		Object value;
		// either only a single value of the type
		if (type == 0) {
			value = nextValue(vars);
		} else {
			// or an array of that type
			if (type < 0)
				type = 1;
			Object[] values = new Object[type];
			for (int i = 0; i < values.length; i++) {
				values[i] = nextValue(vars);
			}
			value = values;
		}

		return value;
	}

	protected Object nextValue(Map<String, Object> vars) throws InPUTException {
		return next(vars);
	}

	private Object nextArray(int[] dimensions, Map<String, Object> vars)
			throws InPUTException {
		Object[] valueArray;
		if (dimensions[0] > 0)
			// create a container for the array of the first dimension
			valueArray = new Object[dimensions[0]];
		else
			// for now, randomly created entries without specific definition,
			// will receive minimum size.
			valueArray = new Object[1];
		// for all dimensions
		for (int i = 0; i < valueArray.length; i++) {
			// call the method again without the first dimension.
			valueArray[i] = next(
					Arrays.copyOfRange(dimensions, 1, dimensions.length), vars);
		}
		return valueArray;
	}

	protected String getSetter() {
		return mapping.getSetter();
	}

	protected String getGetter() {
		return mapping.getGetter();
	}

	@Override
	public void invokeSetter(Object parentValue, Object value)
			throws InPUTException {
		if (setHandle == null)
			setHandle = initSetMethod(parentValue);
		try {
			setHandle.invoke(parentValue, value);
		} catch (IllegalAccessException e) {
			throw new InPUTException(param.getId()
					+ ": You do not have access to the method '"
					+ setHandle.getName() + "'.", e);
		} catch (IllegalArgumentException e) {
			throw new InPUTException(
					param.getId()
							+ ": A method '"
							+ setHandle.getName()
							+ "' with the given arguments does not exist. look over your code mapping file.",
					e);
		} catch (InvocationTargetException e) {
			throw new InPUTException(param.getId()
					+ ": An exception in the method '" + setHandle.getName()
					+ "' has been thrown.", e);
		}
	}

	@Override
	public Object invokeGetter(Object value) throws InPUTException {

		if (getHandle == null)
			getHandle = initGetMethod(value);

		Object fieldValue;
		try {
			fieldValue = getHandle.invoke(value,
					ValueGenerator.EMPTY_OBJECT_ARRAY);
		} catch (IllegalAccessException e) {
			throw new InPUTException(param.getId()
					+ ": You do not have access to the method '"
					+ getHandle.getName() + "'.", e);
		} catch (IllegalArgumentException e) {
			throw new InPUTException(
					param.getId()
							+ ": A method '"
							+ getHandle.getName()
							+ "' with the given arguments does not exist. look over your code mapping file.",
					e);
		} catch (InvocationTargetException e) {
			throw new InPUTException(param.getId()
					+ ": An exception in the method '" + getHandle.getName()
					+ "' has been thrown.", e);
		}
		return fieldValue;
	}

	@SuppressWarnings("unchecked")
	private AMapping initMappings() throws InPUTException {
		IMapping m = null;
		ParamStore store = param.getParamStore();
		if (store != null) {
			IMappings cm = Mappings.getInstance(store.getId());
			if (cm != null)
				m = cm.getMapping(param.getId());
		}

		if (m == null)
			m = initSpecial(m);

		return (AMapping) m;
	}

	private IMapping initSpecial(IMapping m) throws InPUTException {
		if (param instanceof NParam) {
			m = new NumericEmptyMapping(param);
		} else if (param instanceof AStruct) {
			if (((AStruct) param).isStringType())
				m = initStringMapping();
		}
		return m;
	}

	public AMapping initStringMapping() throws InPUTException {
		Element mapping = new Element(Q.MAPPING, Q.MAPPING_NAMESPACE_ID);
		mapping.setAttribute(Q.ID_ATTR, param.getId());
		mapping.setAttribute(Q.CONSTR_ATTR, Q.STRING_TYPE);
		mapping.setAttribute(Q.TYPE_ATTR, Q.STRING_TYPE);
		
		if (param.getParent() instanceof AStruct) {
			mapping.setAttribute(Q.SET_ATTR, Q.SETTER_PREFIX + param.getId());
			mapping.setAttribute(Q.GET_ATTR, Q.GETTER_PREFIX + param.getId());
		}
		
		return init(mapping);
	}

	protected abstract AMapping init(Element mapping) throws InPUTException;

	protected abstract Method initSetMethod(Object parentValue)
			throws InPUTException;

	protected abstract Method initGetMethod(Object parentValue)
			throws InPUTException;

	@Override
	public boolean hasGetHandle() {
		if (hasGetHandle == null)
			initHandles();
		return hasGetHandle;
	}

	private void initHandles() {
		hasGetHandle = mapping.getGetter() != null;
		hasSetHandle = mapping.getSetter() != null;
	}

	@Override
	public boolean hasSetHandle() {
		if (hasSetHandle == null)
			initHandles();
		return hasSetHandle;
	}
	
	@Override
	public void validateInPUT(Object value, ElementCache elementCache)
			throws InPUTException {
		checkConstructorInit();
	}
	
	private void checkConstructorInit() throws InPUTException {
		Element parent = param.getParentElement();
		if (parent.isRootElement())
			return;
		
		if(((Param<?>)parent).initByConstructor(param.getLocalId()))
			throw new InPUTException("The parameter \"" + param.getId() + "\" cannot be set via setter injection because it is instantiated by a constructor.");
	}
}
