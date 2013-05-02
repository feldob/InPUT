package model;

public class AndYetAnotherSecondChoice extends AnotherStructuralParent {

	@SuppressWarnings("unused")
	private SomeChoiceSpecificStructuralSub bar;

	public AndYetAnotherSecondChoice(SomeSharedStructuralSub foo, SomeChoiceSpecificStructuralSub bar) {
		super(foo);
		this.bar = bar;
	}
}