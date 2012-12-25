package model;

public class SomeCommonStructural implements SomeStructural {

	private double customizableSetGetPrimitive;
	
	private Wrapper wrapperPrimitive;
	
	public void customizableSetter(
			double customizableSetGetPrimitive) {
		this.customizableSetGetPrimitive = customizableSetGetPrimitive;
	}
	
	public double andTheCustomizableGetter() {
		return customizableSetGetPrimitive;
	}

	public Wrapper getPrimitive() {
		return wrapperPrimitive;
	}

	public void setPrimitive(Wrapper wrapperPrimitive) {
		this.wrapperPrimitive = wrapperPrimitive;
	}
}
