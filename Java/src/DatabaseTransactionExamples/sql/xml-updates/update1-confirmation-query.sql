/* Querying the database about the value of the structural
 * parameter with the ID "TargetString" in the design with
 * the ID "hello-world-problem-features" */
SELECT
	(xpath(
		'/in:Design/in:SValue[@id="TargetString"]/@value',
		content,
		ARRAY[ARRAY['in', 'http://TheInPUT.org/Design']]))[1]
	AS target_string
FROM input.design
WHERE id = 'hello-world-problem-features';
