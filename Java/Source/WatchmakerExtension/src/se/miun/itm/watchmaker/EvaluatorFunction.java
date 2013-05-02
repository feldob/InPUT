package se.miun.itm.watchmaker;

public interface EvaluatorFunction<T> {

	boolean isNatural();
	
	double evaluate(T candidate);

	int getRuns();

	boolean getDependsOnSuccess();
	
}
