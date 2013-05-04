import java.util.List;
import java.util.Stack;


public class Calculator {
	
	String operators = "+-*/";

	public Integer calculate(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Integer transformToInt(List<String> postfix) {
		Stack<String> stack = new Stack<String>();
		
		while(!postfix.isEmpty()) {
			String tmp = postfix.get(0);
			postfix.remove(0);
			
			
			
		}
		
		return null;
	}
	

}
