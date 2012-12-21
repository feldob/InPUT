package se.miun.itm.input.aspects;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import se.miun.itm.input.model.InPUTException;

public interface Extendable {

	void addContent(String name, ByteArrayOutputStream contentAsStream) throws InPUTException;
	
	Set<String> getContentNames();
	
	ByteArrayOutputStream getContentFor(String identifier);
}

