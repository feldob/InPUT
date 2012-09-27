package se.miun.itm.ga;

import java.math.BigDecimal;

public class Mutation {

	protected BigDecimal length;
	
	private int position;
	
	private Wrapper wrap;
	
	public Mutation(BigDecimal length, Representation representation) {
		this.length = length;
	}
	
	public Wrapper getWrapper() {
		return wrap;
	}
	
	public void setWrapper(Wrapper wrap) {
		this.wrap = wrap;
	}
	
	public void setpositions(Integer position) {
		this.position = position;
	}
	
	public int getpositions() {
		return position;
	}
	
	public BigDecimal getLength() {
		return length;
	}
}