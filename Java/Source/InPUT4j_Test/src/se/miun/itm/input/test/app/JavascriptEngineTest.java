package se.miun.itm.input.test.app;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavascriptEngineTest {

	/**
	 * @param args
	 * @throws ScriptException 
	 */
	public static void main(String[] args) throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager()
		.getEngineByName("JavaScript");
		engine.put("x", 3);
		engine.put("y", 20);
		Object result = engine.eval("x+y");
		System.out.println(result.getClass().getName());
		System.out.println(result);
		result = engine.eval("Math.max(x,y)");
		System.out.println(result.getClass().getName());
		System.out.println(result);
	}
}