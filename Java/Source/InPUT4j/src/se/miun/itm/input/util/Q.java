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
package se.miun.itm.input.util;

import org.jdom.Namespace;
/**
 * A help class that contains all the identifiers of objects and concepts used in InPUT.
 * References should all point here, and none for InPUT meaningful String should be solely defined in another class.
 * @author Felix Dobslaw
 *
 */
public class Q {
	public static final Namespace SCHEMA_INSTANCE_NAMESPACE = Namespace
			.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
	
	public static final String MY_NAMESPACE =  "http://TheInPUT.org/";

	public static final String SCHOICE_TYPE = "SChoiceType";
	
	public static final String MY_NAMESPACE_PREFIX =  "in";
	
	public static final String DESIGN_ROOT = "Design";
	
	public static final String DESIGN_NAMESPACE_ID = MY_NAMESPACE + DESIGN_ROOT;
	
	public static final String DESIGN_SCHEMA_LOCATION = DESIGN_NAMESPACE_ID + " " + DESIGN_NAMESPACE_ID + ".xsd";
	
	public static final Namespace DESIGN_NAMESPACE = Namespace.getNamespace(MY_NAMESPACE_PREFIX, DESIGN_NAMESPACE_ID);
	
	public static final String NVALUE = "NValue";
	
	public static final String SVALUE = "SValue";
	
	public static final String[] DESIGN_ELEMENT_IDS = {NVALUE, SVALUE, DESIGN_ROOT};

	public static final String DESIGN_SPACE_ROOT = "DesignSpace";
	
	public static final String MAPPINGS = "CodeMappings";

	public static final String NPARAM = "NParam";

	public static final String SCHOICE = "SChoice";

	public static final String SPARAM = "SParam";

	public static final String VALUE_ATTR = "value";

	public static final String TYPE_ATTR = "type";

	public static final String ID_ATTR = "id";

	public static final String MAPPING_TYPE = "MappingType";
	
	public static final String MAPPING = "Mapping";

	public static final String DESIGN_SPACE_NAMESPACE_ID = MY_NAMESPACE + DESIGN_SPACE_ROOT;

	public static final Object CODE_MAPPINGS_SCHEMA_LOCATION = MY_NAMESPACE + MAPPINGS + ".xsd";
	
	public static final String DESIGN_SPACE_SCHEMA_LOCATION = DESIGN_SPACE_NAMESPACE_ID + DESIGN_SPACE_NAMESPACE_ID + ".xsd";
	
	public static final Namespace DESIGN_SPACE_NAMESPACE = Namespace.getNamespace(
			MY_NAMESPACE_PREFIX, MY_NAMESPACE + DESIGN_SPACE_ROOT);

	public static final String[] DESIGN_SPACE_ELEMENT_IDS = { NPARAM, SCHOICE, SCHOICE_TYPE,
			SPARAM, DESIGN_SPACE_ROOT };

	public static final String SETTER_PREFIX = "set";
	
	public static final String GETTER_PREFIX = "get";

	public static final String RNG = "rng";

	public static final String SEED = "Seed";

	public static final String EXCL_MIN = "exclMin";

	public static final String EXCL_MAX = "exclMax";

	public static final String INCL_MAX = "inclMax";

	public static final String INCL_MIN = "inclMin";

	public static final String ARRAY_START = "[";
	
	public static final String ESCAPED_ARRAY_START = "\\" + ARRAY_START;

	public static final String REL_MIN_ATTR = "relativeMin";

	public static final String REL_MAX_ATTR = "relativeMax";

	public static final String DEP_ATTR = "dependants";

	public static final String GET_ATTR = "get";

	public static final String SET_ATTR = "set";

	public static final String CONSTR_ATTR = "constructor";
	
	public static final String WRAPPER = "Wrapper";

	public static final String XML = ".xml";

	public static final String ALGORITHM_DESIGN = "algorithmDesign";

	public static final String ALGORITHM_DESIGN_XML = ALGORITHM_DESIGN+XML;
	
	public static final String PROBLEM_FEATURES = "problemFeatures";

	public static final String PROBLEM_FEATURES_XML = PROBLEM_FEATURES+XML;
	
	public static final String PROBLEM_FEATURE_SPACE = "problemSpace";
	
	public static final String PROBLEM_FEATURE_SPACE_XML = PROBLEM_FEATURE_SPACE + XML;
	
	public static final String OUTPUT = "output";
	
	public static final String OUTPUT_XML = OUTPUT+XML;
	
	public static final String ALGORITHM_DESIGN_SPACE = "algorithmSpace";

	public static final String ALGORITHM_DESIGN_SPACE_XML = ALGORITHM_DESIGN_SPACE+ XML;

	public static final String PROPERTY_SPACE = "propertySpace";
	
	public static final String PROPERTY_SPACE_XML = PROPERTY_SPACE+XML;

	public static final String PREFERENCES = "preferences";

	public static final String PREFERENCES_XML = PREFERENCES + XML;
	
	public static final String OUTPUT_SPACE = "outputSpace";

	public static final String OUTPUT_SPACE_XML = OUTPUT_SPACE+XML;

	public static final String CODE_MAPPING = "CodeMapping";

	public static final String RANDOM = "random";

	public static final String FIXED_ATTR = "fixed";

	public static final String MAPPING_ATTR = "mapping";

	public static final String ALGORITHM_MAPPING_XML = ALGORITHM_DESIGN_SPACE + CODE_MAPPING + XML;
	
	public static final String OUTPUT_MAPPING_XML = OUTPUT_SPACE + CODE_MAPPING + XML;
	
	public static final String PROPERTIES_MAPPING_XML = PROPERTY_SPACE+ CODE_MAPPING + XML;
	
	public static final String PROBLEM_MAPPING_XML = PROBLEM_FEATURE_SPACE + CODE_MAPPING + XML;

	public static final String LOGGING = "logging";

	public static final String THREAD_SAFE = "threadSafe";

	public static final String INJECTION = "injection";

	public static final String REF_ATTR = "ref";

	public static final String SYSTEM = "System";

	public static final String LANGUAGE = "Language";

	public static final String JAVA = "java";

	public static final String DEFAULT = "default";

	public static final Object BLANK = Void.TYPE;

	public static final String COMPLEX = "Complex";

	public static final String ADD_ATTR = "add";

	public static final String EXP = ".exp";
	
	public static final String INP = ".inp";

	public static final String CONFIG_ID = "config";

	public static final String CONFIG = CONFIG_ID + XML;
	
	public static final String CONFIG_SPACE = CONFIG_ID + "Space"
			+ XML;
	public static final String CONFIG_MAPPING = CONFIG_ID + "Mapping"
			+ XML;
}