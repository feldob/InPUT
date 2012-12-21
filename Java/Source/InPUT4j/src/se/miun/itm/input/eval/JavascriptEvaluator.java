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
package se.miun.itm.input.eval;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * The standard evaluator, using the integrated javascript engine.
 * 
 * @author Felix Dobslaw
 * 
 * TODO the javascript commands are not mapped for full compliance. (e.g. "Math.max" to "max")
 * 
 * @NotThredSafe
 **/
public class JavascriptEvaluator extends AbstractEvaluator {

	private static final String JAVA_SCRIPT = "JavaScript";

	private static final String DOTS = Pattern.quote(".");

	private static final ScriptEngineManager manager = new ScriptEngineManager();

	private final ScriptEngine engine;

	private final Map<String, String> vars = new HashMap<String, String>();

	public JavascriptEvaluator() {
		engine = manager.getEngineByName(JAVA_SCRIPT);
	}

	@Override
	public void putVariable(String id, Object value) {
		engine.put(get(id), value);
	}

	private String get(String id) {
		if (!vars.containsKey(id))
			vars.put(id, id.replaceAll(DOTS, ""));
		return vars.get(id);
	}

	@Override
	public void clearVariables() {
//		 engine = manager.getEngineByName(JAVA_SCRIPT);
	}

	@Override
	public String evaluate(Set<String> vars,
			String expression) throws ScriptException {
		return engine.eval(replaceVars(expression, vars))
				.toString();
	}

	private String replaceVars(String expression, Set<String> vars) {
		String replaceWith;
		for (String var : vars) {
			replaceWith = this.vars.get(var);
			if (!var.equals(replaceWith))
				expression = expression.replaceAll(Pattern.quote(var), replaceWith);
		}
		return expression;
	}

}
