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

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Namespace;

import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.model.InPUTException;
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
	
	/**
	 * A placeholder for the name of an auto-incremented SQL data type.
	 */
	public static final String SQL_AUTO_INCREMENT_PLACEHOLDER = "AUTO_INCREMENT_PLACEHOLDER";
	
	/**
	 * An immutable {@code Map} in which each key is a DBMS name that
	 * is mapped into an array of SQL functions that shall be part of
	 * the InPUT SQL schema in databases with the specified DBMS.
	 */
	public static final Map<String, String[]> SQL_FUNCTIONS;
	
	/**
	 * The name of the InPUT SQL schema.
	 */
	public static final String SQL_SCHEMA_NAME = "input";
	
	/**
	 * The SQL State code for violation of a unique constraint.
	 */
	public static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
	
	/**
	 * An immutable list of {@link Table}:s that defines the
	 * SQL tables, indexes and rows that InPUT requires.
	 */
	public static final List<Table> SQL_TABLES;
	
	static {
		int charOctetLength = 100;
		Map<String, String[]> sqlFunctions = new LinkedHashMap<String, String[]>(1);
		List<Table> sqlTables = new ArrayList<Table>(9);
		
		try {
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".design_space " +
				"(" +
					"id varchar(" + charOctetLength + ") NOT NULL PRIMARY KEY, " +
					"content xml NOT NULL" +
				")"));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".design " +
				"(" +
					"id varchar(" + charOctetLength + ") NOT NULL PRIMARY KEY, " +
					"design_space varchar(" + charOctetLength + ") NOT NULL REFERENCES " +
						SQL_SCHEMA_NAME + ".design_space(id), " +
					"content xml NOT NULL" +
				")",
				"CREATE INDEX design_design_space " +
				"ON " + SQL_SCHEMA_NAME + ".design(design_space)"));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".programming_language " +
				"(" +
					"name varchar(" + charOctetLength + ") NOT NULL PRIMARY KEY" +
				")",
				null,
				new Table.Row(new Table.Entry<String>(
					"name", Types.VARCHAR, "Java", PreparedStatement.class.getMethod(
						"setString", int.class, String.class)))));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".framework " +
				"(" +
					"id varchar(" + charOctetLength + ") NOT NULL PRIMARY KEY" +
				")"));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".mappings " +
				"(" +
					"id varchar(" + charOctetLength + ") NOT NULL, " +
					"version " + SQL_AUTO_INCREMENT_PLACEHOLDER + " NOT NULL, " +
					"programming_language varchar(" + charOctetLength + ") " +
						"NOT NULL REFERENCES " + SQL_SCHEMA_NAME +
						".programming_language(name), " +
					"framework varchar(" + charOctetLength + ") REFERENCES " +
						SQL_SCHEMA_NAME + ".framework(id), " +
					"design_space varchar(" + charOctetLength + ") NOT NULL REFERENCES " +
						SQL_SCHEMA_NAME + ".design_space(id), " +
					"content xml NOT NULL, " +
					"PRIMARY KEY(id, version), " +
					"UNIQUE(id, programming_language, framework), " +
					"UNIQUE(programming_language, framework, design_space)" +
				")"));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".input " +
				"(" +
					"id varchar(" + charOctetLength + ") NOT NULL PRIMARY KEY, " +
					"algorithm_design_space varchar(" + charOctetLength + ") " +
						"REFERENCES " + SQL_SCHEMA_NAME + ".design_space(id), " +
					"property_space varchar(" + charOctetLength + ") REFERENCES " +
						SQL_SCHEMA_NAME + ".design_space(id), " +
					"problem_feature_space varchar(" + charOctetLength + ") " +
						"REFERENCES " + SQL_SCHEMA_NAME + ".design_space(id), " +
					"output_space varchar(" + charOctetLength + ") REFERENCES " +
						SQL_SCHEMA_NAME + ".design_space(id)" +
				")"));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".experiment " +
				"(" +
					"id varchar(" + charOctetLength + ") NOT NULL PRIMARY KEY, " +
					"input varchar(" + charOctetLength + ") NOT NULL REFERENCES " +
						SQL_SCHEMA_NAME + ".input(id), " +
					"algorithm_design varchar(" + charOctetLength + ") " +
						"REFERENCES " + SQL_SCHEMA_NAME + ".design(id), " +
					"problem_features varchar(" + charOctetLength + ") " +
						"REFERENCES " + SQL_SCHEMA_NAME + ".design(id), " +
					"preferences varchar(" + charOctetLength + ") REFERENCES " +
						SQL_SCHEMA_NAME + ".design(id)" +
				")",
				"CREATE INDEX experiment_input " +
				"ON " + SQL_SCHEMA_NAME + ".experiment(input)"));
			
			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".experiment_content " +
				"(" +
					"experiment varchar(" + charOctetLength + ") NOT NULL REFERENCES " +
						SQL_SCHEMA_NAME + ".experiment(id), " +
					"name varchar(" + charOctetLength + ") NOT NULL, " +
					"content blob NOT NULL, " +
					"PRIMARY KEY(experiment, name)" +
				")"));

			sqlTables.add(new Table(
				"CREATE TABLE " + SQL_SCHEMA_NAME + ".experiment_output " +
				"(" +
					"experiment varchar(" + charOctetLength + ") NOT NULL REFERENCES " +
						SQL_SCHEMA_NAME + ".experiment(id), " +
					"output varchar(" + charOctetLength + ") NOT NULL REFERENCES " +
						SQL_SCHEMA_NAME + ".design(id), " +
					"PRIMARY KEY(experiment, output)" +
				")"));
		} catch (Exception e) {
			// This code should be unreachable.
			e.printStackTrace();
			System.exit(1);
		}
		
		sqlFunctions.put("PostgreSQL", new String [] {
			"CREATE FUNCTION input.update_xml(outdated_xml xml, namespace_mappings text[][2], " +
					"xpath text, newValue text) RETURNS xml AS $$ " +
				"DECLARE " +
					"i int; " +
					"altered_results text[]; " +
					"original_results text[] = ARRAY[XMLSERIALIZE (DOCUMENT (SELECT xpath(" +
						"'.', outdated_xml, namespace_mappings))[1] AS text)]; " +
					"outdated_attribute text; " +
					"partial_xpath text; " +
					"result text; " +
					"result_count int; " +
					"xpath_steps text[] := regexp_split_to_array(xpath, '/'); " +
				"BEGIN " +
					"IF char_length(xpath_steps[1]) > 0 THEN " +
						"i := 1; " +
						"partial_xpath := xpath_steps[i]; " +
					"ELSE " +
						"i := 2; " +
						"partial_xpath := '/' || xpath_steps[i]; " +
					"END IF; " +
					"WHILE i <= array_length(xpath_steps, 1) LOOP " +
						"original_results := original_results || XMLSERIALIZE (CONTENT (SELECT " +
							"xpath(partial_xpath, outdated_xml, namespace_mappings))[1] AS text); " +
						"i := i + 1; " +
						"partial_xpath := partial_xpath || '/' || xpath_steps[i]; " +
					"END LOOP; " +
					"result_count := array_length(original_results, 1); " +
					"outdated_attribute := substring(xpath_steps[result_count] from 2); " +
					"original_results[result_count] := substring(original_results[result_count - 1] from " +
						"outdated_attribute || '\\s*=\\s*\"' || original_results[result_count] || '\"'); " +
					"altered_results := original_results; " +
					"altered_results[result_count] := outdated_attribute || '=\"' || newValue || '\"'; " +
					"FOR i IN REVERSE result_count - 1..1 LOOP " +
						"altered_results[i] := replace(original_results[i], " +
							"original_results[i + 1], altered_results[i + 1]); " +
					"END LOOP; " +
					"RETURN XMLPARSE (DOCUMENT altered_results[1]); " +
				"END; " +
			"$$ LANGUAGE plpgsql"});
		
		SQL_TABLES = Collections.unmodifiableList(sqlTables);
		SQL_FUNCTIONS = Collections.unmodifiableMap(sqlFunctions);
	}
	
	public static String getSchemaLocation() throws InPUTException {
		String result = DESIGN_NAMESPACE_ID + " " + InPUTConfig.getValue(SCHEMA_PATH) + DESIGN_ROOT
				+ ".xsd";
		return result;
	}
}