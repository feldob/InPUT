package se.miun.itm.input.export;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * 
 * @author Felix Dobslaw
 *
 * @ThreadSafe
 */
public class InputProperties extends Properties {

	private static final long serialVersionUID = 1L;

	public InputProperties() {
	}
	
	// ignore the comment and remove the timestamp!
	@Override
	public void store(OutputStream out, String comments) throws java.io.IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out);
		for (String key : stringPropertyNames()) {
			writer.append(key);
			writer.append('=');
			writer.append(getProperty(key));
			writer.append('\n');
		}
		writer.flush();
	};
}