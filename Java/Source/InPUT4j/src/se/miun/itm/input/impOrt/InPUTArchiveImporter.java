/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */
package se.miun.itm.input.impOrt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.aspects.FileNameAssigner;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.util.Q;

/**
 * An importer of InPUT objects, with zip files as sources.
 * @author Felix Dobslaw
 *
 */
public class InPUTArchiveImporter extends FileNameAssigner implements
InPUTImporter<IInPUT> {

	private String inputId = "";

	public InPUTArchiveImporter() {
		super("");
	}

	public InPUTArchiveImporter(String id, String filePath) {
		super(filePath);
		inputId = id;
	}
	
	@Override
	public IInPUT impOrt() throws InPUTException {
		Map<String, InputStream> map = new HashMap<String, InputStream>();
		IInPUT input = null;
		try {
			Enumeration<? extends ZipEntry> entries;
			ZipFile zipFile = new ZipFile(this.fileName);
			entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory())
					map.put(entry.getName(), zipFile.getInputStream(entry));
			}
			initMappings(map);
			input = assembleInPUT(
					map);
			zipFile.close();
		} catch (IOException ioe) {
		}
		return input;
	}
	
	private void initMappings(Map<String, InputStream> map) throws InPUTException {
		try {
		if (map.containsKey(Q.ALGORITHM_MAPPING_XML))
				Mappings.initMapping(map.get(Q.ALGORITHM_MAPPING_XML));
		if (map.containsKey(Q.PROPERTIES_MAPPING_XML))
			Mappings.initMapping(map.get(Q.PROPERTIES_MAPPING_XML));
		if (map.containsKey(Q.PROBLEM_MAPPING_XML))
			Mappings.initMapping(map.get(Q.PROBLEM_MAPPING_XML));
		if (map.containsKey(Q.OUTPUT_MAPPING_XML))
			Mappings.initMapping(map.get(Q.OUTPUT_MAPPING_XML));
		} catch (IOException e) {
			throw new InPUTException(e.getMessage(),e);
		}
	}

	private IInPUT assembleInPUT(Map<String, InputStream> map)
			throws InPUTException {
		IInPUT input;
		input = new InPUT(inputId,
				map.get(Q.ALGORITHM_DESIGN_SPACE_XML),
				map.get(Q.PROPERTY_SPACE_XML),
				map.get(Q.PROBLEM_FEATURE_SPACE_XML),
				map.get(Q.OUTPUT_SPACE_XML));
		return input;
	}

	public void resetId(String inputId) {
		this.inputId  = inputId;
	}

}