package model;

public class SomeStructuralParent {

	private int someSharedPrimitiveSub;
	
	private long anotherSharedPrimitiveSub;
	
	private String someSharedStructuralSub;
	
	public SomeStructuralParent(int someSharedPrimitiveSub) {
		this.someSharedPrimitiveSub = someSharedPrimitiveSub;
	}
	
	public int getSomeSharedPrimitiveSub() {
		return someSharedPrimitiveSub;
	}
	
	public long getAnotherSharedPrimitiveSub() {
		return anotherSharedPrimitiveSub;
	}
	
	public void setSomeSharedStructuralSub(String someSharedStructuralSub) {
		this.someSharedStructuralSub = someSharedStructuralSub;
	}
	
	public String getSomeSharedStructuralSub() {
		return someSharedStructuralSub;
	}
	
	public void setAnotherSharedPrimitiveSub(long anotherSharedPrimitiveSub) {
		this.anotherSharedPrimitiveSub = anotherSharedPrimitiveSub;
	}
}