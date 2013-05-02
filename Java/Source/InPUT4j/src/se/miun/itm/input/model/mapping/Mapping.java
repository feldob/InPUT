/*-- $Copyright (C) 2012 Felix Dobslaw$


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
 */
package se.miun.itm.input.model.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom2.Element;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * An abstract definition of a mapping, which contains common information,
 * developed for maximum code reuse.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public abstract class Mapping implements IMapping {

	protected final String paramId;

	protected final String localId;

	private final String getter;

	private final String setter;

	private final int hash;

	private final Set<IMapping> dependees = new HashSet<IMapping>();

	public Mapping(Element mapping) throws InPUTException {
		this(mapping.getAttributeValue(Q.ID_ATTR), mapping
				.getAttributeValue(Q.SET_ATTR), mapping
				.getAttributeValue(Q.GET_ATTR));
	}

	public Mapping(String paramId, String setter, String getter) {
		this.paramId = paramId;
		hash = paramId.hashCode();
		localId = initLocalId();
		this.getter = initGetSet(Q.GET_ATTR, getter);
		this.setter = initGetSet(Q.SET_ATTR, setter);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	private String initLocalId() {
		String[] chopped = paramId.split(Pattern.quote("."));
		return chopped[chopped.length - 1];
	}

	private String initGetSet(String defauLt, String getSet) {
		if (getSet == null)
			getSet = defauLt + localId;
		else if (getSet.equals("false"))
			getSet = null;
		return getSet;
	}

	@Override
	public String getId() {
		return paramId;
	}

	@Override
	public String getSetter() {
		return setter;
	}

	@Override
	public String getGetter() {
		return getter;
	}

	@Override
	public String getLocalId() {
		return localId;
	}

	@Override
	public void addDependee(IMapping dependee) {
		dependees.add(dependee);
	}

	@Override
	public boolean isIndependent() {
		return dependees.isEmpty();
	}

	@Override
	public boolean dependsOn(IMapping other) {
		return dependees.contains(other) || dependeesDependOn(other);
	}

	private boolean dependeesDependOn(IMapping other) {
		for (IMapping dependant : dependees)
			if (dependant.dependsOn(other))
				return true;
		return false;
	}

	@Override
	public int compareTo(IMapping o) {
		if (dependsOn(o))
			return 1;
		if (o.dependsOn(this))
			return -1;
		return 0;
	}

	@Override
	public List<IMapping> getDependees() {
		return new ArrayList<IMapping>(dependees);
	}
}