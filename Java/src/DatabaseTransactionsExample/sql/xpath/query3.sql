/* Querying the database about which experiments have
 * an output design with a numeric parameter that has
 * the ID "Y" and a value lesser than 0.27 */

SELECT * FROM  
	(SELECT experiment, id AS design,
		(SELECT (xpath( 
			'/in:Design/in:NValue[@id="Y"][@value<0.27]/@value',  
			content,  
			ARRAY[ARRAY['in', 'http://TheInPUT.org/Design']]))[1])
		AS y  
	FROM input.experiment_output, input.design  
	WHERE output = id)
	AS output_design  
WHERE y IS NOT NULL
