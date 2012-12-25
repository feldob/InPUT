package model;

public class YetAnotherThirdChoice extends SomeStructuralParent {

	private float someChoiceSpecificPrimitiveSub;

	public YetAnotherThirdChoice(int someSharedPrimitiveSub, float someChoiceSpecificPrimitiveSub) {
		super(someSharedPrimitiveSub);
		this.someChoiceSpecificPrimitiveSub = someChoiceSpecificPrimitiveSub;
	}
	
	public float getSomeChoiceSpecificPrimitiveSub() {
		return someChoiceSpecificPrimitiveSub;
	}
}