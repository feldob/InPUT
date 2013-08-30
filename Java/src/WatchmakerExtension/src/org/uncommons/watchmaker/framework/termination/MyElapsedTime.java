package org.uncommons.watchmaker.framework.termination;

import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

public class MyElapsedTime implements TerminationCondition {
	private final long maxTimeForExecutionInMS;

	public MyElapsedTime(long maxTimeForExecutionInMS) {
		assert maxTimeForExecutionInMS > 0;
		this.maxTimeForExecutionInMS = maxTimeForExecutionInMS;
	}

	public boolean shouldTerminate(PopulationData<?> populationData) {
		return populationData.getElapsedTime() >= maxTimeForExecutionInMS;
	}

	public long getMaxTimeForExecutionInMS() {
		return maxTimeForExecutionInMS;
	}
}