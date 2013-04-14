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
package se.miun.itm.input.export;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.FileNameAssigner;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.util.Q;

/**
 * exports design spaces or spaces to LaTeX tables.
 * 
 * @author Felix Dobslaw
 * 
 * @NotThreadSafe
 * 
 */
public class LaTeXFileExporter extends FileNameAssigner implements Exporter<Void> {

	private final static String LINEBREAK = System.getProperty("line.separator");

	public LaTeXFileExporter() {
		this("");
	}

	public LaTeXFileExporter(String path) {
		super(path);
	}

	@Override
	public Void export(Document xml) throws InPUTException {
		if (xml == null)
			throw new IllegalArgumentException("There has to be a context Document given for export.");

		String type = xml.getRootElement().getName();
		if (type.equals(Q.DESIGN_SPACE_ROOT)) {
			exportDesignSpace(xml);
		} else if (type.equals(Q.DESIGN_ROOT)) {
			exportDesign(xml);
		} else if (type.equals(Q.MAPPINGS)) {
			exportCodeMappings(xml);
		} else
			throw new IllegalArgumentException("The document you supplied is not of InPUT type because the root element has the name '"
					+ type + "', which is not supported.");
		return null;
	}

	private void exportCodeMappings(Document xml) {
		throw new IllegalArgumentException("The method is not supported for code mappings (yet).");

	}

	private void exportDesign(Document xml) throws InPUTException {
		String id = xml.getRootElement().getAttributeValue(Q.ID_ATTR);

		StringBuilder b = new StringBuilder();
		appendDesignTableStart(b, id);

		List<Element> values = xml.getRootElement().getChildren();
		for (Element value : values) {
			b.append(LINEBREAK);
			b.append("\\hline");
			b.append(LINEBREAK);
			appendValueToTable(b, value, 0);
		}
		appendDesignTableEnd(b, id);

		writeFile(b);
	}

	private void appendValueToTable(StringBuilder b, Element value, int rootDistance) {
		setId(b,value, rootDistance);
		// always get the value and print the id to it simply, check for children then!
		if (value.getAttribute(Q.VALUE_ATTR) != null) {
			if (value.getName().equals(Q.NVALUE)) {
				b.append('$');
				b.append(value.getAttributeValue(Q.VALUE_ATTR));
				b.append('$');
			} else if (value.getName().equals(Q.SVALUE)) {
				appendStructuralValue(b, value, rootDistance);
			}
		} else {
			List<Element> children = value.getChildren();
			int depth = getDepth(value);
			if (depth <= 2) {
				b.append("$\\left(");
				b.append(LINEBREAK);
				b.append("\\begin{array}{");
				appendCs(b, getAmountElements(children));
				b.append('}');
				b.append(LINEBREAK);
				appendMatrixContent(b, children, depth, rootDistance);
				b.append("\\end{array}");
				b.append(LINEBREAK);
				b.append("\\right)$");
			} else {
				b.append("Too high dimensional to visualize.");
			}
		}
		if (rootDistance == 1) {
			b.append("\\\\");
			b.append(LINEBREAK);
		}
	}

	private void setId(StringBuilder b,Element value, int rootDistance) {
		boolean close = false;
		if (rootDistance > 0)
		{
			b.append("\\hspace{");
			b.append(rootDistance*2);
			b.append("mm}");
			if (rootDistance % 2 == 1) {
				close = true;
				b.append("\\textit{");
			}
		}
		
		b.append(value.getAttributeValue(Q.ID_ATTR));
		if (close)
			b.append('}');	
		b.append(" & ");
	}

	private int getAmountElements(List<Element> children) {
		List<Element> newChildren = children;
		while (!newChildren.get(0).getChildren().isEmpty()) {
			newChildren = newChildren.get(0).getChildren();
		}
		return newChildren.size();
	}

	private void appendStructuralValue(StringBuilder b, Element value, int rootDistance) {
		
		String valueString = value.getAttributeValue(Q.VALUE_ATTR); 
		b.append(valueString);
		b.append("\\\\");
		b.append(LINEBREAK);
		List<Element> children = value.getChildren();
		if (!children.isEmpty())
			for (Element child : children)
				appendValueToTable(b, child, rootDistance+1);
	}

	private void appendMatrixContent(StringBuilder b, List<Element> children, int depth, int rootDistance) {
		int count = 0;
		for (Element child : children) {
			appendMatrixRow(b, child, depth, rootDistance);
			if (count < children.size() - 1)
				b.append("\\\\");
			count++;
		}
	}

	private void appendMatrixRow(StringBuilder b, Element child, int depth, int rootDistance) {
		if (child.getAttributeValue(Q.VALUE_ATTR) == null)
			appendMatrixRow(b, child.getChildren().get(0), depth, rootDistance);
		else {
			if (depth == 1)
				appendValueToTable(b, child, rootDistance);
			else {
				List<Element> values = child.getParentElement().getChildren();
				for (int i = 0; i < values.size(); i++) {
					appendValueToTable(b, values.get(i), rootDistance);
					if (i < values.size() - 1)
						b.append('&');
				}
			}
		}
	}

	private int getDepth(Element value) {
		int count = 0;
		Element child = value;
		while (!child.getChildren().isEmpty()) {
			if (child.getAttributeValue(Q.VALUE_ATTR) != null)
				break;
			count++;
			child = child.getChildren().get(0);
		}
		return count;
	}

	private void appendCs(StringBuilder b, int depth) {
		for (int i = 0; i < depth; i++)
			b.append('c');
	}

	private void appendDesignTableStart(StringBuilder b, String id) {
		b.append("\\begin{table}");
		b.append(LINEBREAK);
		b.append("\\centering");
		b.append(LINEBREAK);
		b.append("\\begin{tabular}{|l|r|}");
		b.append(LINEBREAK);
		b.append("\\hline");
		b.append(LINEBREAK);
		b.append("\\textbf{Parameter} & \\textbf{Value} \\\\");
		b.append(LINEBREAK);
		b.append("\\hline");
		b.append(LINEBREAK);
	}

	private void appendDesignTableEnd(StringBuilder b, String id) {
		b.append("\\hline");
		b.append(LINEBREAK);
		b.append("\\end{tabular}");
		b.append(LINEBREAK);
		b.append("\\caption{The parameter values for design \\textit{");
		b.append(id);
		b.append("}.}");
		b.append(LINEBREAK);
		b.append("\\label{table:design-");
		b.append(id);
		b.append('}');
		b.append(LINEBREAK);
		b.append("\\end{table}");
	}


	private void exportDesignSpace(Document xml) throws InPUTException {
		String id = xml.getRootElement().getAttributeValue(Q.ID_ATTR);

		StringBuilder b = new StringBuilder();
		appendDesignSpaceTableStart(b, id);

		List<Element> params = xml.getRootElement().getChildren();
		for (Element param : params) {
			b.append(LINEBREAK);
			b.append("\\hline");
			b.append(LINEBREAK);
			appendParamToTable(b, param, id);
		}
		appendDesignSpaceTableEnd(b, id);

		writeFile(b);
	}

	private void writeFile(StringBuilder b) throws InPUTException {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.append(b.toString());
			bw.close();
		} catch (IOException e) {
			throw new InPUTException("The file by name '" + fileName + "' could not be written successfully.", e);
		}
	}

	private void appendParamToTable(StringBuilder b, Element param, String id) throws InPUTException {

		String fixed = getFixedValue(param);

		if (!param.getName().equals(Q.SCHOICE)) {
			appendParamId(b, param);
			b.append(" & \\texttt{");
			b.append(getTypeString(param));
			b.append("} & ");
			if (isNonComplexFixed(fixed))
				b.append("\\textbf{");
			b.append(getRangeString(param, fixed));
			if (isNonComplexFixed(fixed))
				b.append("}");
			b.append("\\\\");
			b.append(LINEBREAK);
			b.append("\\hline");
			b.append(LINEBREAK);
		}
		appendChildren(b, param, id, fixed);
	}

	private boolean isNonComplexFixed(String fixed) {
		return fixed != null && !fixed.contains(" ");
	}

	private String getFixedValue(Element param) {
		String fixed = param.getAttributeValue(Q.FIXED_ATTR);
		if (fixed == null && !Q.STRING.equals(param.getAttributeValue(Q.TYPE_ATTR))) {
			if (param.getName().equals(Q.SPARAM)) {
				List<Element> choices = getChoiceChildren(param);
				if (choices.isEmpty())
					fixed = param.getAttributeValue(Q.ID_ATTR); 
				else if (choices.size() == 1)
					fixed = choices.get(0).getAttributeValue(Q.ID_ATTR);
			}
		}
		return fixed;
	}

	private List<Element> getChoiceChildren(Element param) {
		List<Element> choices = new ArrayList<Element>();
		List<Element> children = param.getChildren();
		for (Element child : children)
			if (child.getName().equals(Q.SCHOICE))
				choices.add(child);
		return choices;
	}

	private void appendChildren(StringBuilder b, Element param, String id, String fixed) throws InPUTException {
		List<Element> children = param.getChildren();
		for (Element child : children) {
			if (child.getName().equals(Q.SCHOICE)) {
				if (isNonComplexFixed(fixed) && !child.getAttributeValue(Q.ID_ATTR).equals(fixed))
					continue;
			} else
				b.append("\\hspace{2 mm}");
			appendParamToTable(b, child, id);
		}
	}

	private void appendParamId(StringBuilder b, Element param) {
		String paramId = param.getAttributeValue(Q.ID_ATTR);
		if (param.getParentElement().getName().equals(Q.SCHOICE)) {
			b.append("\\textit{");
			b.append(param.getParentElement().getAttributeValue(Q.ID_ATTR));
			b.append("}.");
		}
		b.append(paramId);
	}

	private String getRangeString(Element param, String fixed) throws InPUTException {
		String value = null;
		if (isNonComplexFixed(fixed)) {
				value = fixed;
		} else if (param.getName().equals(Q.SPARAM))
			value = getStructRangeString(param);
		else if (param.getName().equals(Q.NPARAM))
			value = getNumericalRange(param);
		else {
			throw new InPUTException("The given parameter element cannot be recognized as such by InPUT.");
		}
		return value;
	}

	private String getNumericalRange(Element original) throws InPUTException {
		Element clone = original.clone();
		new Element("something").addContent(clone);
		NParam element = new NParam(clone, "something", null);
		element.addMaxDependency(element);
		element.addMinDependency(element);
		element.initRanges();

		return "$" + element.toString() + "$";
	}

	private static String getStructRangeString(Element param) {
		StringBuilder b = new StringBuilder();
		if (!param.getChildren().isEmpty()) {
			b.append("\\{");
			Element subParamE;
			int amountStruct = 0;
			for (Object subParam : param.getChildren()) {
				subParamE = (Element) subParam;
				if (subParamE.getName().equals(Q.SCHOICE)) {
					b.append(subParamE.getAttributeValue(Q.ID_ATTR));
					b.append(", \\\\ &&");
					amountStruct++;
				}
			}
			if (amountStruct != 0) {
				b.delete(b.length() - 7, b.length());
			} else {
				param.getAttributeValue(Q.DEFAULT);
			}
			b.append("\\}");
		}
		return b.toString();
	}

	private String getTypeString(Element param) throws InPUTException {
		String value = null;
		if (param.getName().equals(Q.SPARAM)) {
			String type = param.getAttributeValue(Q.TYPE_ATTR);
			if (Q.STRING.equals(type))
				value = Q.STRING;
			else
				value = initStructuralString(type);
		} else if (param.getName().equals(Q.NPARAM))
			value = param.getAttributeValue(Q.TYPE_ATTR);
		else {
			throw new InPUTException("The given parameter element can not be recognized as such by InPUT.");
		}
		return value;
	}

	private String initStructuralString(String type) {
		String value = "struct";
		if (type != null) {
			int firstApp = type.indexOf('[');
			if (firstApp != -1)
				value = value + type.substring(firstApp, type.length());
		}
		return value;
	}

	private void appendDesignSpaceTableEnd(StringBuilder b, String id) {
		b.append("\\end{tabular}");
		b.append(LINEBREAK);
		b.append("\\caption{The parameter ranges for design space \\textit{");
		b.append(id);
		b.append("} (Constants in bold font).}");
		b.append(LINEBREAK);
		b.append("\\label{table:paramranges-");
		b.append(id);
		b.append('}');
		b.append(LINEBREAK);
		b.append("\\label{table:design-space-");
		b.append(id);
		b.append('}');
		b.append(LINEBREAK);
		b.append("\\end{table}");
	}

	private void appendDesignSpaceTableStart(StringBuilder b, String id) {
		b.append("\\begin{table}");
		b.append(LINEBREAK);
		b.append("\\centering");
		b.append(LINEBREAK);
		b.append("\\begin{tabular}{|l|l|c|}");
		b.append(LINEBREAK);
		b.append("\\hline");
		b.append(LINEBREAK);
		b.append("\\textbf{Parameter} & \\textbf{Type} & \\textbf{Range} \\\\");
	}

	@Override
	public Void export(Exportable input) throws InPUTException {
		throw new IllegalArgumentException("The method is not supported for a complete InPUT object (yet).");
	}
}