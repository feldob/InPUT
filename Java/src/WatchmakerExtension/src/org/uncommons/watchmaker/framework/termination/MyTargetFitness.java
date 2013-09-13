package org.uncommons.watchmaker.framework.termination;

import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

public class MyTargetFitness implements TerminationCondition
{
    private final double targetFitness;
    private final boolean natural;

    public MyTargetFitness(double targetFitness, boolean natural)
    {
        this.targetFitness = targetFitness;
        this.natural = natural;
    }

    public boolean shouldTerminate(PopulationData<?> populationData)
    {
        if (natural)
            return populationData.getBestCandidateFitness() >= targetFitness;
        else
            return populationData.getBestCandidateFitness() <= targetFitness;
    }
    
    public double getTargetFitness() {
		return targetFitness;
	}
}
