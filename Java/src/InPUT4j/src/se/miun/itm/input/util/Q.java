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
 */package se.miun.itm.input.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom2.Namespace;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.sql.DatabaseAdapter;
import se.miun.itm.input.util.sql.Table;

/**
 * A help class that contains all the identifiers of objects and concepts used
 * in InPUT. References should all point here, and none for InPUT meaningful
 * String should be solely defined in another class.
 * 
 * @author Felix Dobslaw
 * @author Stefan Karlsson
 */
public class Q {
	public static final Namespace SCHEMA_INSTANCE_NAMESPACE = Namespace
			.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

	public static final String SCHEMA_LOCATION_ATTR = "schemaLocation";

	public static final String SCHOICE_TYPE = "SChoiceType";

	public static final String MY_NAMESPACE_PREFIX = "in";

	public static final String DESIGN_ROOT = "Design";

	public static final String NVALUE = "NValue";

	public static final String SVALUE = "SValue";

	public static final String[] DESIGN_ELEMENT_IDS = { NVALUE, SVALUE,
			DESIGN_ROOT };

	public static final String DESIGN_SPACE_ROOT = "DesignSpace";

	public static final String MAPPINGS = "CodeMappings";

	public static final String NPARAM = "NParam";

	public static final String SCHOICE = "SChoice";

	public static final String SPARAM = "SParam";

	public static final String VALUE_ATTR = "value";

	public static final String TYPE_ATTR = "type";

	public static final String ID_ATTR = "id";
	
	public static final String VERSION_ATTR = "version";

	public static final String MAPPING_TYPE = "MappingType";

	public static final String MAPPING = "Mapping";

	public static final String[] DESIGN_SPACE_ELEMENT_IDS = { NPARAM, SCHOICE,
			SCHOICE_TYPE, SPARAM, DESIGN_SPACE_ROOT };

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

	public static final String ALGORITHM_DESIGN_XML = ALGORITHM_DESIGN + XML;

	public static final String PROBLEM_FEATURES = "problemFeatures";

	public static final String PROBLEM_FEATURES_XML = PROBLEM_FEATURES + XML;

	public static final String PROBLEM_FEATURE_SPACE = "problemSpace";

	public static final String PROBLEM_FEATURE_SPACE_XML = PROBLEM_FEATURE_SPACE
			+ XML;

	public static final String OUTPUT = "output";

	public static final String OUTPUT_XML = OUTPUT + XML;

	public static final String ALGORITHM_DESIGN_SPACE = "algorithmSpace";

	public static final String ALGORITHM_DESIGN_SPACE_XML = ALGORITHM_DESIGN_SPACE
			+ XML;

	public static final String PROPERTY_SPACE = "propertySpace";

	public static final String PROPERTY_SPACE_XML = PROPERTY_SPACE + XML;

	public static final String PREFERENCES = "preferences";

	public static final String PREFERENCES_XML = PREFERENCES + XML;

	public static final String OUTPUT_SPACE = "outputSpace";

	public static final String OUTPUT_SPACE_XML = OUTPUT_SPACE + XML;

	public static final String CODE_MAPPING = "CodeMapping";

	public static final String RANDOM = "random";

	public static final String FIXED_ATTR = "fixed";

	public static final String MAPPING_ATTR = "mapping";

	public static final String ALGORITHM_MAPPING_XML = ALGORITHM_DESIGN_SPACE
			+ CODE_MAPPING + XML;

	public static final String OUTPUT_MAPPING_XML = OUTPUT_SPACE + CODE_MAPPING
			+ XML;

	public static final String PROPERTIES_MAPPING_XML = PROPERTY_SPACE
			+ CODE_MAPPING + XML;

	public static final String PROBLEM_MAPPING_XML = PROBLEM_FEATURE_SPACE
			+ CODE_MAPPING + XML;

	public static final String LOGGING = "logging";

	public static final String THREAD_SAFE = "threadSafe";

	public static final String INJECTION = "injection";

	public static final String REF_ATTR = "ref";

	public static final String SYSTEM = "System";

	public static final String LANGUAGE = "Language";

	public static final String JAVA = "java";

	public static final String DEFAULT = "default";

	public static final Object BLANK = Void.TYPE;
	
	public static final Object PLACEHOLDER = "...";

	public static final String COMPLEX = "Complex";

	public static final String ADD_ATTR = "add";

	public static final String EXP = ".exp";

	public static final String INP = ".inp";

	public static final String CONFIG_ID = "config";

	public static final String CONFIG = CONFIG_ID + XML;

	public static final String CONFIG_SPACE = CONFIG_ID + "Space" + XML;
	public static final String CONFIG_MAPPING = CONFIG_ID + "Mapping" + XML;

	public static final String NAMESPACE_ID = "http://TheInPUT.org/";

	public static final String DESIGN_NAMESPACE_ID = NAMESPACE_ID + DESIGN_ROOT;

	public static final String DESIGN_SPACE_NAMESPACE_ID = NAMESPACE_ID
			+ DESIGN_SPACE_ROOT;

	public static final String MAPPING_NAMESPACE_ID = NAMESPACE_ID + CODE_MAPPING;
	
	public static final Namespace DESIGN_SPACE_NAMESPACE = Namespace
			.getNamespace(MY_NAMESPACE_PREFIX, NAMESPACE_ID + DESIGN_SPACE_ROOT);

	public static final Namespace DESIGN_NAMESPACE = Namespace.getNamespace(
			MY_NAMESPACE_PREFIX, DESIGN_NAMESPACE_ID);

	public static final String SCHEMA_PATH = "schemaPath";

	public static final String RUNTIME_VALIDATION = "runtimeValidation";

	public static final String STRING = "String";

	public static final String STRING_TYPE = "java.lang.String";

	public static final Object[] DEFAULT_STRING_ACTUAL_PARAMS = {DEFAULT};

	public static final String EVALUATOR = "evaluator";

	public static final String NULL = "null";

	public static final String CACHE_DESIGNS = "cacheDesigns";

	public static final String OPTIONAL = "optional";

<<<<<<< HEAD
	/**
	 * The pattern used for delimiting SQL function definitions in SQL files.
	 */
	public static final Pattern SQL_FUNCTION_DELIMITER =
		Pattern.compile("(?i)\\s*--\\s*FUNCTION[ \t]+DELIMITER[ \t-]*\n");
	
	/**
	 * An immutable set of SQL statements that inserts data that InPUT requires.
	 */
	public static final Set<String> SQL_DATA;
	
	/**
	 * An immutable set of the definitions of the functions of the InPUT SQL schema.
	 */
	public static final Set<String> SQL_FUNCTIONS;
	
	/**
	 * An immutable set of the definitions of the indexes of the InPUT SQL schema.
	 */
	public static final Set<String> SQL_INDEXES;
	
	/**
	 * An immutable set of {@link Table}:s that defines
	 * the SQL tables of the InPUT SQL schema.
	 */
	public static final Set<Table> SQL_TABLES;
	
	/**
	 * The name of the InPUT SQL schema.
	 */
	public static final String SQL_SCHEMA_NAME = "input";
	
	/**
	 * The SQL State code for violation of a unique constraint.
	 */
	public static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
	
	static {
		final String	DATA_FILENAME = "data.sql",
						FUNCTION_FILENAME = "functions.sql",
						INDEX_FILENAME = "indexes.sql",
						TABLE_FILENAME = "tables.sql";
		Map<String, Set<String>> sqlStatements = new LinkedHashMap<String, Set<String>>();
		Pattern delimiterPattern = Pattern.compile("\\s*;+\\s*");
		Scanner sqlScanner = null;
		Set<Table> tables = new LinkedHashSet<Table>();
		
		try {
			
			for (String filename : Arrays.asList(
					TABLE_FILENAME, INDEX_FILENAME, FUNCTION_FILENAME, DATA_FILENAME)) {
				Set<String> statements = new LinkedHashSet<String>();
				
				sqlScanner = new Scanner(DatabaseAdapter.class.
					getResourceAsStream(filename), "UTF-8");
				sqlScanner.useDelimiter(filename.equals(FUNCTION_FILENAME) ?
					SQL_FUNCTION_DELIMITER : delimiterPattern);
				
				while (sqlScanner.hasNext())
					statements.add(sqlScanner.next());
				
				sqlScanner.close();
				sqlStatements.put(filename, statements);
			}
			
			for (String def : sqlStatements.get(TABLE_FILENAME))
				tables.add(new Table(def));
			
		} catch (Exception e) {
			// This code should be unreachable.
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (sqlScanner != null)
				sqlScanner.close();
		}
		
		SQL_TABLES = Collections.unmodifiableSet(tables);
		SQL_INDEXES = Collections.unmodifiableSet(sqlStatements.get(INDEX_FILENAME));
		SQL_FUNCTIONS = Collections.unmodifiableSet(sqlStatements.get(FUNCTION_FILENAME));
		SQL_DATA = Collections.unmodifiableSet(sqlStatements.get(DATA_FILENAME));
	}
	
=======
>>>>>>> upstream/master
	public static String getSchemaLocation() throws InPUTException {
		String result = DESIGN_NAMESPACE_ID + " " + InPUTConfig.getValue(SCHEMA_PATH) + DESIGN_ROOT
				+ ".xsd";
		return result;
	}
}