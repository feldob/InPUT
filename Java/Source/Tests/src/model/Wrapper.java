package model;

public class Wrapper {

	private Double primitive;
	
	public Wrapper(double primitive) {
		this.primitive = primitive;
	}
	
	public Double toValue() {
		return primitive;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Wrapper))
			return false;
		return primitive == ((Wrapper)obj).toValue();
	}
}
