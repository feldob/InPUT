package se.miun.itm.input.tuning.converter;

public 	class ParamValuePair {

	public final String paramId;

	public final String value;

	public ParamValuePair(String param, String value) {
		this.paramId = param;
		this.value = value;
	}

	@Override
	public String toString() {
		return "[ " + paramId + ": " + value + " ]";
	}
}