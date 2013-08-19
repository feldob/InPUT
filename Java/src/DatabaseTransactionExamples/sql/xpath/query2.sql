/* Querying the database about which experiment has the output
 * design with the greatest value of the numeric subparameter
 * "Fitness" of the structural parameter "Best" */
SELECT * FROM  
	(SELECT experiment, id AS design_id,  
		CAST (XMLSERIALIZE (CONTENT (SELECT (xpath( 
			'/in:Design/in:SValue[@id="Best"]/in:NValue[@id="Fitness"]/@value',  
			content,  
			ARRAY[ARRAY['in', 'http://TheInPUT.org/Design']]))[1])  
		AS varchar) AS numeric) AS fitness  
	FROM input.experiment_output, input.design  
	WHERE output = id)
	AS output_design  
WHERE fitness IS NOT NULL  
ORDER BY fitness DESC  
LIMIT 1
