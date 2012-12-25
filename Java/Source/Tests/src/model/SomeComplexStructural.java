package model;

import java.util.ArrayList;
import java.util.List;

public class SomeComplexStructural implements SomeAbstractComplexStructural {

	List<SomeAbstractComplexStructural> list = new ArrayList<SomeAbstractComplexStructural>();
	
	public SomeComplexStructural() {
		
	}
	
	public void addEntry(SomeAbstractComplexStructural entry) {
		list.add(entry);
	}
	
	public int size(){
		return list.size();
	}
	
	//TODO this has to be coupled to the tests; yet its not supported!
	public List<SomeAbstractComplexStructural> getEntries() {
		return list;
	}

	public SomeAbstractComplexStructural getEntry(int i) {
		return list.get(i);
	}
}