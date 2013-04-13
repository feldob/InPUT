package org.uncommons.watchmaker.framework;

import java.util.List;

public class MyPopulationData<T> {

	private List<EvaluatedCandidate<T>> evaluatedCandidates;
	private PopulationData<T> data;
	
	public MyPopulationData(PopulationData<T> data, List<EvaluatedCandidate<T>> evaluatedCandidates) {
		this.evaluatedCandidates = evaluatedCandidates;
		this.data = data;
	}
	
	public PopulationData<T> getData() {
		return data;
	}

	public List<EvaluatedCandidate<T>> getEvaluatedCandidates() {
		return evaluatedCandidates;
	}
}