package se.miun.itm.input.eval;

import java.util.Set;

import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import se.miun.itm.input.model.param.NParam;

/**
 * @author Felix Dobslaw
 */
public class JEvalEvaluator extends AbstractEvaluator {

	private final Evaluator eval = new Evaluator();

	@Override
	public void putVariable(String id, Object value) {
		eval.putVariable(id, value.toString());
	}

	@Override
	public void clearVariables() {
	}

	@Override
	public String evaluate(NParam param, Set<String> vars,
			String expression) throws EvaluationException {
		String result;
		expression = toJEval(vars, expression);
		// ensure that the value is of the right type! Does not have
		// dangling zeros etc. (may happen with jeval)
		result = eval.evaluate(expression);
		result = ensureType(param.getNumericType(), result);
		return result;
	}

	private static String toJEval(Set<String> vars, String expression) {
		for (String var : vars)
			expression = expression.replaceAll(var, "#{" + var + "}");
		return expression;
	}

}