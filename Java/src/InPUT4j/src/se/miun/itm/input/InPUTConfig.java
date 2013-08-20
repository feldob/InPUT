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
 */package se.miun.itm.input;

import java.io.InputStream;
import java.util.List;

import org.jdom2.Element;

import se.miun.itm.input.impOrt.InPUTImporter;
import se.miun.itm.input.impOrt.InputStreamImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * The static IInPUT service provider that is initiated each time InPUT starts. InPUT itself
 * is used for its configuration, which is why it, at startup, requires to read
 * the InPUT configuration files in this folder. The InPUT properties can either
 * be retrieved by calling the methods below, or by direct access through the
 * scope of any design or experiment.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public abstract class InPUTConfig {
	
	private static IDesign config; // the config file used; by default config.xml in this folder

	private static Document configDoc; // the plain config file document xml.

	/**
	 * Initiate InPUTConfig by automatically loading configuration space, mapping and design.
	 */
	static {
		try {
			// read the code mapping file
			InputStream configSpaceStream = IInPUT.class
					.getResourceAsStream(Q.CONFIG_SPACE);
			// import the design space
			IDesignSpace configSpace = new DesignSpace(configSpaceStream,
					IInPUT.class.getResourceAsStream(Q.CONFIG_MAPPING));
			InPUTImporter<Document> importer = new InputStreamImporter(
					IInPUT.class.getResourceAsStream(Q.CONFIG), false);
			// import the design
			config = configSpace.impOrt(importer);

		} catch (InPUTException e) {
			System.out
					.println("The InPUT configuration is broken. Please consult the InPUT team.");
			e.printStackTrace();
		}
	}

	/**
	 * retrieves the value with the given paramId from the InPUT config design.
	 * 
	 * @param paramId
	 * @return
	 * @throws InPUTException
	 */
	public static <T> T getValue(String paramId) throws InPUTException {
		return config.getValue(paramId);
	}

	/**
	 * If runtimeValidation is set to "true" then the schemaLocation has to point to a reachable position with the
	 * Design.xsd¸DesignSpace.xsd, and CodeMapping.xsd in place. Validation adds support for error handling.
	 * @return
	 * @throws InPUTException
	 */
	public static boolean isValidationActive() throws InPUTException {
		return config.getValue(Q.RUNTIME_VALIDATION);
	}

	/**
	 * retrieves the String version of the value with the given paramId from the
	 * InPUT config design.
	 * 
	 * @param paramId
	 * @return
	 * @throws InPUTException 
	 */
	public static String getValueToString(String paramId) throws InPUTException {
		return config.getValueToString(paramId);
	}

	/**
	 * checks if logging is activated for InPUT. If activated, a "input.log" file should appear in the working directory.
	 * 
	 * @return
	 */
	public static boolean isLoggingActive() {
		return Boolean.parseBoolean(getProperty(Q.LOGGING));
	}

	/**
	 * Returns the requested value from the config design. It initializes the
	 * configuration in case it has not been set yet.
	 * 
	 * @param type
	 * @return
	 */
	private static String getProperty(String type) {
		if (config == null) {
			try {
				return initProperties(type);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				return config.getValue(type).toString();
			} catch (InPUTException e) {
				// TODO remove for production
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * initialize the config.xml document tree.
	 * 
	 * @param type
	 * @return
	 * @throws InPUTException
	 */
	private static String initProperties(String type) throws InPUTException {
		if (configDoc == null)
			configDoc = SAXUtil.build(
					IInPUT.class.getResourceAsStream(Q.CONFIG), false);

		List<Element> preferences = configDoc.getRootElement().getChildren();
		for (Element param : preferences)
			if (param.getAttributeValue(Q.ID_ATTR).equals(type))
				return param.getAttributeValue(Q.VALUE_ATTR);
		return null;
	}

	/**
	 * Is the currently running version of InPUT thread safe?
	 * 
	 * @return
	 */
	public static boolean isThreadSafe() {
		return Boolean.parseBoolean(getProperty(Q.THREAD_SAFE));
	}

	/**
	 * Does the currently running version support code injection using the Get,
	 * Set, Input, Output annotations?
	 * 
	 * @return
	 */
	public static boolean isInjectionActive() {
		return Boolean.parseBoolean(getProperty(Q.INJECTION));
	}

	/**
	 * returns the config file xml tree in pretty print.
	 * 
	 * @return
	 */
	public static String getPrettyConfig() {
		return config.toString();
	}

	/**
	 * extends the given design file to the config file. By default, this should
	 * happen to each newly created instance of type Design.
	 * 
	 * @param design
	 */
	public static void extendToConfigScope(IDesign design) {
		design.extendScope(config);
	}

	/**
	 * experimentally: Being able to set the config values is discouraged, and should only be used
	 * if you know what you are doing.
	 * @param paramId
	 * @param value
	 * @throws InPUTException
	 */
	public static void setValue(String paramId, Object value) throws InPUTException {
		 config.setValue(paramId, value);
	}

	/**
	 * Checks if all designs that are created can be retrieved using the Design.lookup method.
	 * @return
	 */
	public static boolean cachesDesigns() {
		return Boolean.parseBoolean(getProperty(Q.CACHE_DESIGNS));
	}
}