package se.miun.itm.input.tuning;

import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesignSpace;

public abstract class Tuner implements ITuner {

	private static final String SINGLE_OBJECTIVE_SPACE = "singleObjectiveSpace.xml";

	protected final IInPUT input;

	protected String studyId;

	private static IDesignSpace outputSpace;

	public Tuner(IInPUT input, String studyId) throws InPUTException {
		this.studyId = studyId;
		this.input = input;
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
}