import java.util.Scanner;


public class GUI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		
		System.out.println("Skriv ett matematiskt uttryck:");
		String math;
		math = in.next();
		Calculator calc = new Calculator();
		try {
			System.out.println(calc.calculate(math));
		} catch (ParserErrorException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
	}

	
}
