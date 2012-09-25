package se.miun.itm.input.example.inputoutput;

import se.miun.itm.input.annotation.Get;
import se.miun.itm.input.annotation.Input;
import se.miun.itm.input.annotation.Output;
import se.miun.itm.input.annotation.Set;

/**
 * The user friendly version of importing experimental assumptions, executing an
 * experiment, and then storing the results in an output file, without a lot of
 * boilerplate code. For that sake, a combination of Annotations and Aspect
 * oriented programming is used. The main difference is that it allows a
 * declarative way of describing the experiments, in contrast to the procedural
 * appraoch in the traditional example.
 * 
 * @author Felix Dobslaw
 * 
 */
public class InputOutputInjection {

	@Get(value = "paramId", from = "inputId")
	private Object[] input;

	private Object[] output = new Object[input.length];

	@Input(id = "inputId", file = "input.xml")
	@Output(id = "outputId", file = "output.xml", spaceFile = "someSpace.xml")
	public static void main(String[] args) {
		new InputOutputInjection();
	}

	public InputOutputInjection() {
		doSomething();
	}

	@Set(value = "paramId", of = "outputId", to = "output")
	private void doSomething() {
		if (input != null) {
			for (int i = 0; i < input.length; i++) {
				System.out.println(input[i]);
				output[i] = input[i];
			}
		} else
			System.out.println("empty!");
	}
}