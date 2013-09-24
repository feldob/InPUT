/**************************************Function**************************************
 * NOTE:																			*
 *     This function is not part of the "standard" InPUT SQL schema, it is only		*
 *     created in PostgreSQL databases												*
 * Name: input.update_xml															*
 * Purpose: Updates a node of a XML document										*
 * Arguments:																		*
 *     xpath_exp text:																*
 *         An XPath expression that uniquely identifies the node that shall be		*
 *         updated																	*
 *     xml_document xml: The XML document that shall be updated						*
 *     namespace_mappings text[][2]:												*
 *         An array of namespace mappings, where each mapping consists of			*
 *         a two-dimensional array whose first element is the namespace				*
 *         name and whose second second element is the namespace URI				*
 *     new_node text: The new node													*
 * Returns: The updated XML document												*
 ************************************************************************************/
CREATE FUNCTION input.update_xml(
	xpath_exp text,
	xml_document xml,
	namespace_mappings text[][2],
	new_node text)
RETURNS xml AS $$
DECLARE
	i int;
	step_count int;
	outdated_attr text;
	outdated_node text;
	partial_xpath text := '/';
	result_string text;
	xpath_steps text[] := regexp_split_to_array(regexp_replace(xpath_exp, '^/+', ''), '/+');
	xpath_results xml[] := xpath(xpath_exp, xml_document, namespace_mappings);
BEGIN
	IF array_length(xpath_results, 1) <> 1 THEN
		RAISE EXCEPTION 'The XPath expression doesn''t uniquely identify a node: %',
			xpath_exp USING ERRCODE = 'invalid_parameter_value';
	END IF;
	xml_document := (xpath('.', xml_document, namespace_mappings))[1];
	step_count := array_length(xpath_steps, 1);
	IF position('@' in xpath_steps[step_count]) = 1 THEN
		outdated_attr := substring(xpath_steps[step_count] from 2);
		outdated_node := outdated_attr || '="' || XMLSERIALIZE (CONTENT xpath_results[1] AS text) || '"';
		new_node := outdated_attr || '="' || new_node || '"';
	ELSE
		outdated_node := XMLSERIALIZE (CONTENT xpath_results[1] AS text);
	END IF;
	FOR i IN REVERSE greatest(1, step_count - 1)..1 LOOP
		partial_xpath := overlay(partial_xpath placing '//' || xpath_steps[i] from 1 for 1);
		xpath_results := xpath(partial_xpath, xml_document, namespace_mappings);
		IF array_length(xpath_results, 1) = 1 THEN
			result_string := XMLSERIALIZE (CONTENT xpath_results[1] AS text);
			IF step_count - i < 2 THEN
				RETURN XMLPARSE (CONTENT replace(
					XMLSERIALIZE (CONTENT xml_document AS text),
					result_string,
					replace(result_string, outdated_node, new_node)));
			ELSE
				RETURN input.update_xml(
					partial_xpath,
					xml_document,
					namespace_mappings,
					replace(result_string, outdated_node, new_node));
			END IF;
		END IF;
	END LOOP;
	RAISE EXCEPTION 'Unsupported XPath expression: %', xpath_exp USING ERRCODE = 'feature_not_supported';
END;
$$ LANGUAGE plpgsql;

-- FUNCTION DELIMITER

