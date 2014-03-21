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
 */package se.miun.itm.input.tuning;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public abstract class Tuner implements ITuner {

	private static final String SINGLE_OBJECTIVE_SPACE = "singleObjectiveSpace.xml";

	protected final IInPUT input;

	protected String studyId;

	private final boolean minProblem;

	private static IDesignSpace outputSpace;

	public Tuner(IInPUT input, String studyId, boolean minProblem) throws InPUTException {
		this.studyId = studyId;
		this.input = input;
		this.minProblem = minProblem;
		if (input.getAlgorithmDesignSpace() == null)
			throw new InPUTException(
					"You have to explicitly set an input context with an algorithm design space.");
	}

	public static IDesignSpace getSingleObjectiveSpace() {
		if (outputSpace == null)
			initOutputSpace();
		return outputSpace;
	}

	private static void initOutputSpace() {
		try {
			outputSpace = new DesignSpace(
					Tuner.class.getResourceAsStream(SINGLE_OBJECTIVE_SPACE));
		} catch (InPUTException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isMinProblem() {
		return minProblem;
	}
}