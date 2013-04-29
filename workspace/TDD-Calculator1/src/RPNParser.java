import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

public class RPNParser {
	String numbers = "0123456789";
	String operators = "+-*/";
	String brackets = "()";

	public List<String> InfixToRPN(String expression) throws ParserErrorException {
		List<String> parsedString = ParseString(expression);

		List<String> returnValue = CreatePostFix(parsedString);

		return returnValue;
	}

	private List<String> CreatePostFix(List<String> parsedString) throws ParserErrorException {
		List<String> output = new ArrayList<String>();
		Stack<String> operatorStack = new Stack<String>();
		boolean foundBracket = false;

		for (int i = 0; i < parsedString.size(); i++) {
			String tmpString = parsedString.get(i);
			if(operators.contains(tmpString) && i == 0){
				throw new ParserErrorException("En operator hittad först");
			}
			if(operators.contains(tmpString) && i == parsedString.size()-1){
				throw new ParserErrorException("En operator hittad sist");
			}

			if (operators.indexOf(tmpString) != -1) {
				if (operatorStack.empty()) {
					operatorStack.push(tmpString);
				} else {
					String tmpOp = operatorStack.pop();
					if (brackets.contains(tmpOp)) {
						operatorStack.push(tmpOp);
						operatorStack.push(tmpString);
						continue;
					}
					if (tmpString.equals("+") || tmpString.equals("-")) {
						output.add(tmpOp);
						operatorStack.push(tmpString);
					} else {
						if (tmpOp.equals("*") || tmpOp.equals("/")) {
							output.add(tmpOp);
							operatorStack.push(tmpString);
						} else {
							operatorStack.push(tmpOp);
							operatorStack.push(tmpString);
						}

					}
				}

			} else if (brackets.indexOf(tmpString) != -1) {
				if (tmpString.equals("(")) {
					operatorStack.push(tmpString);
					foundBracket = true;
				} else if (tmpString.equals(")") && foundBracket) {
					String tmpOperator = operatorStack.pop();
					foundBracket = false;
					while (!tmpOperator.equals("(")) {
						output.add(tmpOperator);
						tmpOperator = operatorStack.pop();
					}
				} else {
					throw new ParserErrorException("Fel bracket hittad först");
				}
			} else {
				output.add(tmpString);
			}
		}
		

		while (!operatorStack.empty()) {
			output.add(operatorStack.pop());
		}

		if(foundBracket){
			throw new ParserErrorException("Sista bracketen inte hittad");
		}
		return output;
	}

	private List<String> ParseString(String expression) {
		List<String> returnValue = new ArrayList<String>();

		expression = expression.replaceAll("\\s+", "");
		StringTokenizer tokenizer = new StringTokenizer(expression, "+-*/()",
				true);
		while (tokenizer.hasMoreTokens()) {
			returnValue.add(tokenizer.nextToken());
		}

		return returnValue;
	}

}
