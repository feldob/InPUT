package se.miun.itm.ga;

import java.sql.Time;

public class LocalSearch {

	private int i;
	private Helper helper;

	public LocalSearch(Object test, int i, Time time) {
		this.i = i;
	}
	
	public void sethelper(Helper helper) {
		this.helper = helper;
	}
	
	public Helper gethelper() {
		return helper;
	}
	
	public LocalSearch(Object test) {
	}
	
	public LocalSearch() {
	}

	public int getValue() {
		return i;
	}
}