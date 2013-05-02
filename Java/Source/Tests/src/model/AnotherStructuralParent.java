package model;

public class AnotherStructuralParent {

	private SomeSharedStructuralSub foo;

	public AnotherStructuralParent(SomeSharedStructuralSub foo) {
		this.foo = foo;
	}
	
	public SomeSharedStructuralSub getSomeSharedStructuralSub() {
		return foo;
	}
}