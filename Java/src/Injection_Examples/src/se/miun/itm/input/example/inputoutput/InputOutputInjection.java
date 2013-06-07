/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package se.miun.itm.input.example.inputoutput;

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
 * approach in the traditional example.
 * 
 * @author Felix Dobslaw
 * 
 */
public class InputOutputInjection {

	@Get(value = "paramId", from = "inputId")
	private int[] input;

	private int[] output = new int[input.length];

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