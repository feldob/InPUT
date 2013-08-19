/* Querying the database about which experiments have a problem
 * feature design with a structural parameter that has the ID
 * "WSN" and a numeric subparameter which in turn has the ID
 * "UpperBoundTDMASize" and a value greater than 80 */
SELECT * FROM  
	(SELECT experiment.id AS experiment, design.id AS design,  
		(SELECT (xpath( 
			'/in:Design/in:SValue[@id="WSN"]/in:NValue[@id="UpperBoundTDMASize"][@value>80]/@value',  
			content,  
			ARRAY[ARRAY['in', 'http://TheInPUT.org/Design']]))[1])  
		AS upper_bound_tdma_size  
	FROM input.experiment, input.design  
	WHERE problem_features = design.id)
	AS problem_feature_design  
WHERE upper_bound_tdma_size IS NOT NULL
