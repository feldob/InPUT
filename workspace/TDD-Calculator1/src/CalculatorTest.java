import static org.junit.Assert.*;

import org.junit.Test;


public class CalculatorTest {

	@Test
	public void test() {
		Integer expected = 252;
		Calculator calculator = new Calculator();
		
		Integer actual = new Integer(0);
		try {
			actual = calculator.calculate("5*(27+3*7) + 22-(5*3)+10/2");
		} catch (ParserErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(expected, actual);

	}
	
	

}

