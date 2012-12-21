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
package se.miun.itm.input.util.xml;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.util.Q;

/**
 * An engine that allows the processing of xpath expressions, in an easy to use
 * fassion.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 */
public class XPathProcessor {

	private static final Filter<Element> ELEMENT_FILTER = new ElementFilter();

	public static List<Element> query(String expression, Namespace nameSpace,
			Object context) throws InPUTException {
		XPathFactory inst;
		inst = XPathFactory.instance();
		expression = correctNamespaceInPUT(expression, nameSpace);

		XPathExpression<Element> expr = inst.compile(expression,
				ELEMENT_FILTER, null, nameSpace);
		return expr.evaluate(context);
	}

	public static String correctNamespaceGiveInPUT(String expression) {
		for (int i = 0; i < Q.DESIGN_ELEMENT_IDS.length; i++) {
			expression = expression.replace(Q.DESIGN_ELEMENT_IDS[i],
					Q.DESIGN_NAMESPACE.getPrefix() + ":"
							+ Q.DESIGN_ELEMENT_IDS[i]);
		}
		return expression;
	}

	public static String correctNamespaceInPUT(String expression,
			Namespace nameSpace) {
		if (!nameSpace.equals(Namespace.NO_NAMESPACE)) {
			for (int i = 0; i < Q.DESIGN_SPACE_ELEMENT_IDS.length; i++) {
				expression = expression.replace(Q.DESIGN_SPACE_ELEMENT_IDS[i],
						Q.DESIGN_SPACE_NAMESPACE.getPrefix() + ":"
								+ Q.DESIGN_SPACE_ELEMENT_IDS[i]);
			}
		}
		return expression;
	}
}
