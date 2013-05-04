import java.util.List;
import java.util.Stack;

public class Calculator {

	String operators = "+-*/";

	public Integer calculate(String string) throws ParserErrorException {
		RPNParser parser = new RPNParser();
		List<String> postfix = parser.InfixToRPN(string);
		return transformToInt(postfix);

	}

	private Integer transformToInt(List<String> postfix) {
		Stack<String> stack = new Stack<String>();

		while (!postfix.isEmpty()) {
			String tmp = postfix.get(0);
			postfix.remove(0);
			if (operators.contains(tmp)) {
				String nbr1 = stack.pop();
				String nbr2 = stack.pop();
				stack.push(calculate(nbr2, nbr1, tmp));
			} else {
				stack.push(tmp);
			}
		}

		return Integer.parseInt(stack.pop());
	}

	private String calculate(String nbr1, String nbr2, String operator) {
		Integer firstNbr = Integer.parseInt(nbr1);
		Integer secondNbr = Integer.parseInt(nbr2);

		if (operator.equals("+")) {
			Integer tmpInt = firstNbr + secondNbr;
			return tmpInt.toString();
		} else if (operator.equals("-")) {
			Integer tmpInt = firstNbr - secondNbr;
			return tmpInt.toString();
		} else if (operator.equals("*")) {
			Integer tmpInt = firstNbr * secondNbr;
			return tmpInt.toString();
		} else if (operator.equals("/")) {
			Integer tmpInt = firstNbr / secondNbr;
			return tmpInt.toString();
		}
		return "";
	}

}
