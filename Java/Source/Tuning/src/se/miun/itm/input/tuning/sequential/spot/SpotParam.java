package se.miun.itm.input.tuning.sequential.spot;

public class SpotParam {

	public final String type;

	public final String id;

	public final String high;

	public final String low;

	public SpotParam(String roiString) {
		String[] entries = roiString.split(" ");
		id = entries[0].trim();
		low = entries[1].trim();
		high = entries[2].trim();
		type = entries[3].trim();
	}

	public SpotParam(String id, String high, String low, String type) {
		this.id = id;
		this.low = low;
		this.high = high;
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpotParam))
			return false;

		SpotParam foreigner = (SpotParam) obj;
		if (!foreigner.id.equals(id) || !foreigner.high.equals(high)
				|| !foreigner.low.equals(low) || !foreigner.type.equals(type))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(id);
		b.append(' ');
		b.append(low);
		b.append(' ');
		b.append(high);
		b.append(' ');
		b.append(type);
		return b.toString();
	}
}