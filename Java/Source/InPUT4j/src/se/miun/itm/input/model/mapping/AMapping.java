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

import java.util.regex.Pattern;

import se.miun.itm.input.util.Q;

/**
 * An abstract definition of a mapping, which contains common information,
 * developed for maximum code reuse.
 * 
 * @author felix
 * 
 */
public abstract class AMapping implements IMapping {

	protected final String paramId;
	protected final String localId;
	private final String getter;
	private final String setter;

	private final boolean hasGetHandle;

	private final boolean hasSetHandle;

	private final int hash;

	public AMapping(String paramId, String setter, String getter) {
		this.paramId = paramId;
		hash = paramId.hashCode();
		localId = localId();
		this.getter = initGetSet(Q.GET_ATTR, getter);
		hasGetHandle = this.getter == null ? false : true;
		this.setter = initGetSet(Q.SET_ATTR, setter);
		hasSetHandle = this.setter == null ? false : true;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	private String localId() {
		String[] chopped = paramId.split(Pattern.quote("."));
		return chopped[chopped.length - 1];
	}

	protected String initGetSet(String defauLt, String getSet) {
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
	public boolean hasGetHandle() {
		return hasGetHandle;
	}

	@Override
	public boolean hasSetHandle() {
		return hasSetHandle;
	}

	@Override
	public String getLocalId() {
		return localId;
	}
}