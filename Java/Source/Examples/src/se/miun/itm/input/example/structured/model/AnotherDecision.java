package se.miun.itm.input.example.structured.model;

import java.math.BigDecimal;

public class AnotherDecision {

	private double proportionBurned;
	
	private BigDecimal temperature;
	
	public AnotherDecision() {
		proportionBurned = .3;
		temperature = new BigDecimal(50);
	}
	
	public void setProportionBurned(double proportionBurned) {
		this.proportionBurned = proportionBurned;
	}
	
	public void setTemperature(BigDecimal temperature) {
		this.temperature = temperature;
	}
	
	public double getProportionBurned() {
		return proportionBurned;
	}
	
	public BigDecimal getTemperature() {
		return temperature;
	}
}
