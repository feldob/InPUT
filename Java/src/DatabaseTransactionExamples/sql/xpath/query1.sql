/* Querying the database about which experiments have a problem
 * feature design with a structural parameter that has the ID
 * "WSN" and a numeric subparameter which in turn has the ID
 * "UpperBoundTDMASize" and a value greater than 80 */
SELECT experiment, design, upper_bound_tdma_size[1] FROM  
	(SELECT experiment.id AS experiment, design.id AS design,  
		xpath( 
			'/in:Design/in:SValue[@id="WSN"]/in:NValue[@id="UpperBoundTDMASize"][@value>80]/@value',  
			content,  
			ARRAY[ARRAY['in', 'http://TheInPUT.org/Design']])
		AS upper_bound_tdma_size  
	FROM input.experiment, input.design  
	WHERE problem_features = design.id)
	AS problem_feature_design  
WHERE array_length(upper_bound_tdma_size, 1) > 0
