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
package se.miun.itm.input.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.FileNameAssigner;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.util.Q;

/**
 * Exports the given experiment or InPUT to a zip file.
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe (name and exporter are related!)
 */
public class ZipFileExporter extends FileNameAssigner implements
		Exporter<Void> {

	private ByteArrayExporter byteExporter;

	public ZipFileExporter() {
		this("");
	}
	
	public ZipFileExporter(String fileName) {
		super(fileName);
		byteExporter = new ByteArrayExporter();
	}

	@Override
	public synchronized Void export(Exportable input) throws InPUTException {
		try {
			exportToStream(input);
		} catch (FileNotFoundException e) {
			throw new InPUTException(
					"A problem creating an output stream to file by name "
							+ fileName + ".", e);
		} catch (IOException e) {
			throw new InPUTException(
					"A problem creating an output stream to file by name "
							+ fileName + ".", e);
		}
		return null;
	}

	private OutputStream exportToStream(Exportable input)
			throws InPUTException, IOException {

		fileName = new File(this.fileName).getAbsolutePath();
		ZipOutputStream zipFile = null;
		OutputStream os = null;
		if (input instanceof IExperiment) {
			preprocessName(Q.EXP);
			os = new FileOutputStream(fileName);
			zipFile = new ZipOutputStream(os);
			exportExperiment((IExperiment) input, zipFile);
		} else if (input instanceof IInPUT) {
			preprocessName(Q.INP);
			os = new FileOutputStream(fileName);
			zipFile = new ZipOutputStream(os);
			exportSetup((IInPUT) input, zipFile);
			exportMappings(input, zipFile);
		}

		zipFile.close();

		return os;
	}

	private void exportExperiment(IExperiment input, ZipOutputStream zipFile)
			throws InPUTException {
		
		addZipEntry(zipFile, input.getAlgorithmDesign(),
				Q.ALGORITHM_DESIGN_XML);
		addZipEntry(zipFile, input.getProblemFeatures(),
				Q.PROBLEM_FEATURES_XML);
		addZipEntry(zipFile, input.getPreferences(), Q.PREFERENCES_XML);
		
		// export the output!
		List<IDesign> output = input.getOutput();
		for (int i = 0; i < output.size(); i++)
			addZipEntry(zipFile, output.get(i), Q.OUTPUT + (i + 1)
					+ Q.XML);
		
		addContent(input, zipFile);
	}

	private void addContent(IExperiment input, ZipOutputStream zipFile) throws InPUTException {
		for (String contentName : input.getContentNames()) {
			addZipEntry(zipFile, input.getContentFor(contentName), contentName);
		}
	}

	private void preprocessName(String extension) {
		if (!(fileName.contains(extension) || fileName.contains(Pattern.quote("."))))
			fileName += extension;
	}

	private void exportSetup(IInPUT input, ZipOutputStream zipFile)
			throws InPUTException {
		
		addZipEntry(zipFile, input.getAlgorithmDesignSpace(),
				Q.ALGORITHM_DESIGN_SPACE_XML);
		addZipEntry(zipFile, input.getProblemFeatureSpace(),
				Q.PROBLEM_FEATURE_SPACE_XML);
		addZipEntry(zipFile, input.getPropertySpace(),
				Q.PROPERTY_SPACE_XML);
		addZipEntry(zipFile, input.getOutputSpace(),
				Q.OUTPUT_SPACE_XML);
	}

	private void exportMappings(Exportable exportable,
			ZipOutputStream zipFile) throws InPUTException {
		IInPUT input = getInPUT(exportable);

		addZipEntry(input.getAlgorithmDesignSpace(), Q.ALGORITHM_MAPPING_XML, zipFile);
		addZipEntry(input.getPropertySpace(), Q.PROPERTIES_MAPPING_XML, zipFile);
		addZipEntry(input.getProblemFeatureSpace(), Q.PROBLEM_MAPPING_XML, zipFile);
		addZipEntry( input.getOutputSpace(), Q.OUTPUT_MAPPING_XML, zipFile);
	}

	private void addZipEntry(IDesignSpace space,
			String codeMappingRef, ZipOutputStream zipFile) throws InPUTException {
		if (space != null)
			addZipEntry(zipFile, Mappings.getInstance(space.getId()),
					codeMappingRef);
		
	}

	private IInPUT getInPUT(Exportable input) {
		if (input instanceof IInPUT)
			return (IInPUT) input;
		else
			return ((IExperiment) input).getInPUT();
	}

	private void addZipEntry(ZipOutputStream zipfile, Exportable design,
			String fileName) throws InPUTException {
		if (design != null)
			addZipEntry(zipfile, design.export(byteExporter), fileName);
	}

	private void addZipEntry(ZipOutputStream zipfile, ByteArrayOutputStream os, String fileName) throws InPUTException {
		try {
			zipfile.putNextEntry(new ZipEntry(fileName));
		} catch (IOException e) {
			throw new InPUTException("The entry for '" + fileName
					+ "' could not be added to the zip file '" + zipfile
					+ ".", e);
		}
		try {
			zipfile.write(os.toByteArray());
		} catch (IOException e) {
			throw new InPUTException("The entry for '" + fileName
					+ "' could not be written to the zip file '" + zipfile
					+ ".", e);
		}
	}

	@Override
	public Void export(Document xml) throws InPUTException {
		throw new InPUTException(
				"exporting an xml document a zip file is not supported. Look for the XMLFileExporter instead.");
	}
}