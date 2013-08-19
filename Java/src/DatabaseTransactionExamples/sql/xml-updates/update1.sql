/* Changes the value to "HELLO STEFAN" for the structural
 * parameter with the ID "TargetString" in the design
 * with the ID "hello-world-problem-features" */
UPDATE input.design
SET content = input.update_xml(
	content,
	'{{"in", "http://TheInPUT.org/Design"}}',
	'/in:Design/in:SValue[@id="TargetString"]/@value',
	'HELLO STEFAN')
WHERE id = 'hello-world-problem-features';
