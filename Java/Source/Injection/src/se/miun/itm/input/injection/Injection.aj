package se.miun.itm.input.injection;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.FieldSignature;
import org.aspectj.lang.reflect.MethodSignature;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.annotation.Get;
import se.miun.itm.input.annotation.Input;
import se.miun.itm.input.annotation.Output;
import se.miun.itm.input.annotation.Set;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

public privileged aspect Injection {

	public static final Map<String, IDesign> designs = new HashMap<String, IDesign>();
	
	private static final Map<Annotation, Object> visited = new HashMap<Annotation, Object>();

	pointcut getField() : (get(@Get * *.*)) && if(InPUTConfig.isInjectionActive());

	pointcut setFieldConstructor() : execution(@Set *.new(..)) && if(InPUTConfig.isInjectionActive());

	pointcut setFieldMethod() : execution(@Set * *.*(..)) && if(InPUTConfig.isInjectionActive());

	pointcut initOutputDesignConstructor() : execution(@Output *.new(..))&& if(InPUTConfig.isInjectionActive());

	pointcut initOutputDesignMethod() : execution(@Output * *.*(..)) && if(InPUTConfig.isInjectionActive());

	pointcut initInputDesignConstructor() : execution(@Input *.new(..)) && if(InPUTConfig.isInjectionActive());

	pointcut initInputDesignMethod() : execution(@Input * *.*(..))&& if(InPUTConfig.isInjectionActive());

	after() : setFieldConstructor() || setFieldMethod() {
		Set setAnn = (Set)getAnnotation(Set.class, thisJoinPointStaticPart);
		// read the field value
		Object theObject = thisJoinPoint.getThis();
		Field field;
		try {
			field = theObject.getClass().getDeclaredField(setAnn.to());
		field.setAccessible(true);
		Object to;
		to = field.get(theObject);
		designs.get(setAnn.of()).setValue(setAnn.value(), to);
		} catch (Exception e) {
			// TODO remove for production
			e.printStackTrace();
		}
	}

	before() : getField() {
		Field field = ((FieldSignature) thisJoinPointStaticPart.getSignature())
				.getField();
		field.setAccessible(true);
		Get getAnn = field.getAnnotation(Get.class);

		Object value = null;
		if (!visited.containsKey(getAnn)) {
			IDesign design = designs.get(getAnn.from());
			try {
				value = design.getValue(getAnn.value());
				visited.put(getAnn, value);
				field.set(thisJoinPoint.getThis(), value);
			} catch (Exception e) {
				// TODO remove for production
				e.printStackTrace();
			}
		}
		
	}

	before() : initOutputDesignMethod() || initOutputDesignConstructor(){
		Output outputAnn = (Output) getAnnotation(Output.class,
				thisJoinPointStaticPart);

		IDesign design = null;
		// first try to read an output file
		File designFile = new File(outputAnn.file());
		if (designFile.exists())
			try {
				design = new Design(outputAnn.file());
			} catch (InPUTException e) {
				// TODO remove for production
				e.printStackTrace();
			}else{
				IDesignSpace space;
				try {
					space = new DesignSpace(outputAnn.spaceFile());
					design = space.nextDesign(outputAnn.id());
				} catch (InPUTException e) {
					// TODO remove for production
					e.printStackTrace();
				}
			}
		
		designs.put(outputAnn.id(), design);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Annotation getAnnotation(Class clazz, JoinPoint.StaticPart joinPoint) {
		Signature signature = joinPoint.getSignature();
		Annotation ann = null;
		if (signature instanceof MethodSignature) {
			ann = ((MethodSignature) signature).getMethod()
					.getAnnotation(clazz);
		} else if (signature instanceof ConstructorSignature) {
			ann = ((ConstructorSignature) signature).getConstructor()
					.getAnnotation(clazz);
		}
		return ann;
	}

	before() : initInputDesignMethod()|| initInputDesignConstructor(){
		Input inputAnn = (Input) getAnnotation(Input.class,
				thisJoinPointStaticPart);
		IDesign design = null;
		try {
			design = new Design(inputAnn.file());
		} catch (InPUTException e) {
			// TODO remove for production
			e.printStackTrace();
		}
		designs.put(inputAnn.id(), design);
	}
}