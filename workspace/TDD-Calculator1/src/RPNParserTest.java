import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class RPNParserTest {

	@Test
	public void InfixToRPNTest() {
		List<String> expected = Arrays.asList("5", "27", "3", "7", "*", "+",
				"*", "22", "+");
		RPNParser rpnParser = new RPNParser();

		List<String> actual = new ArrayList<String>();
		try {
			actual = rpnParser.InfixToRPN("5*(27+3*7)+22");
		} catch (ParserErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(expected.equals(actual));

	}

	@Test(expected = ParserErrorException.class)
	public void testExceptionFirstBracket() throws ParserErrorException {
		RPNParser rpnParser = new RPNParser();
		List<String> actual = rpnParser.InfixToRPN(")5*(27+3*7)+22");

	}
	
	@Test(expected = ParserErrorException.class)
	public void testExceptionLastBracket() throws ParserErrorException {
		RPNParser rpnParser = new RPNParser();
		List<String> actual = rpnParser.InfixToRPN("5*(27+3*7+22");

	}
	
	@Test(expected = ParserErrorException.class)
	public void testExceptionLastOperator() throws ParserErrorException {
		RPNParser rpnParser = new RPNParser();
		List<String> actual = rpnParser.InfixToRPN("5*(27+3*7)+22-");

	}
	
	@Test(expected = ParserErrorException.class)
	public void testExceptionFirstOperator() throws ParserErrorException {
		RPNParser rpnParser = new RPNParser();
		List<String> actual = rpnParser.InfixToRPN("+5*(27+3*7)+22");

	}
}
