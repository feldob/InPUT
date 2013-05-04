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
 */package se.miun.itm.input.impOrt;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import se.miun.itm.input.aspects.FileNameAssigner;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.InputStreamWrapper;
import se.miun.itm.input.util.Q;
/**
 * An importer of experiments, with zip files as sources.
 * 
 * @author Felix Dobslaw
 *
 * @NotThreadSafe
 */
public class ExperimentArchiveImporter extends FileNameAssigner
implements InPUTImporter<Map<String, InputStreamWrapper>> {

	public ExperimentArchiveImporter() {
		super("");
	}

	public ExperimentArchiveImporter(String filePath) {
		super(filePath);
	}
	
	@Override
	public Map<String, InputStreamWrapper> impOrt() throws InPUTException {
		Map<String, InputStreamWrapper> map = new HashMap<String, InputStreamWrapper>();
		try {
			Enumeration<? extends ZipEntry> entries;
			ZipFile zipFile = new ZipFile(fileName);
			entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				map.put(entry.getName(),new InputStreamWrapper(zipFile.getInputStream(entry)));
			}
			zipFile.close();
		} catch (IOException ioe) {
			throw new InPUTException("The zip file could not be read from the given position: " + fileName, ioe);
		}
		return map;
	}

	public static boolean isExperimentalFile(String entry) {
		
		if (entry.startsWith(Q.OUTPUT) && entry.endsWith(Q.XML))
			return true;
		
		if (entry.equals(Q.PROBLEM_FEATURES_XML) || entry.equals(Q.ALGORITHM_DESIGN_XML) || entry.equals(Q.PREFERENCES_XML))
			return true;
		
		return false;
	}
}
