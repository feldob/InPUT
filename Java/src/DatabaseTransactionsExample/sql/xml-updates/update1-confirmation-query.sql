/* Querying the database about the value of the structural
 * parameter with the ID "TargetString" in the design with
 * the ID "hello-world-problem-features" */
SELECT XMLSERIALIZE (CONTENT (SELECT xpath(
	'/in:Design/in:SValue[@id="TargetString"]/@value',
	(SELECT content FROM input.design WHERE id = 'hello-world-problem-features'),
	ARRAY[ARRAY['in', 'http://TheInPUT.org/Design']]))[1] AS varchar)
AS target_string;
