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
package se.miun.itm.input.model.element;

import org.jdom2.Element;
import org.jdom2.Namespace;

import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.util.ParamUtil;

/**
 * The most general type of InPUT Xml elements. The standard XML implementations of jdom have been extended to carry
 * the necessary meta data for an easy tree treatment.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public abstract class InPUTElement extends Element implements Identifiable {

	private static final long serialVersionUID = 3900176771742934942L;

	// can change identity, for performance purposes
	private String fullId;

	protected InPUTElement(String name, Namespace designNamespace,
			Param<?> param, Element original) {
		super(name, designNamespace);
		fullId = ParamUtil.deriveValueReferenceId(original, param);
	}

	protected InPUTElement(Element original) {
		fullId = ParamUtil.deriveParamId(original);
	}

	void setFullId(String fullId) {
		this.fullId = fullId;
	}

	@Override
	public String getId() {
		return fullId;
	}

	@Override
	public String toString() {
		return getName() + ": " + fullId;
	}
}