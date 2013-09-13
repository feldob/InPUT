package se.miun.itm.input.lazy;

import java.util.Iterator;

import se.miun.itm.input.IExperiment;

/**
 * An abstract datatype which lazily initializes all experiments for a given context.
 * 
 * @author Felix Dobslaw
 */
public interface LazyExperimentInitializer extends Iterator<IExperiment> {

	public IExperiment next();
	
	public int size();

}