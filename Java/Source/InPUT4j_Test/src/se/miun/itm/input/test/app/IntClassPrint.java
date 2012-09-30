package se.miun.itm.input.test.app;

public class IntClassPrint {

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		System.out.println(Class.forName("int").toString());
		System.out.println(Integer.class.getName());
	}

}
