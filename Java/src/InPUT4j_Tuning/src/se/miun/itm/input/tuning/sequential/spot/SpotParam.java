/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package se.miun.itm.input.tuning.sequential.spot;

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