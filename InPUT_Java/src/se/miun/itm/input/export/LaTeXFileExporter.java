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
import java.util.List;

import org.jdom.Element;

import se.miun.itm.input.aspects.FileNameAssigner;
import se.miun.itm.input.aspects.InPUTExportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.Ranges;
import se.miun.itm.input.model.param.NParam;
import se.miun.itm.input.util.Q;

/**
 * exports design spaces or spaces to LaTeX tables.
 * @author Felix Dobslaw
 *
 */
public class LaTeXFileExporter extends FileNameAssigner implements
		InPUTExporter<Void> {

	private final static String linebreak = System
			.getProperty("line.separator");

	public LaTeXFileExporter() {
		super("");
	}

	public LaTeXFileExporter(String path) {
		super(path);
	}

	@Override
	public Void export(Document xml) throws InPUTException {
		if (xml == null)
			throw new IllegalArgumentException(
					"There has to be a context Document given for export.");

		String type = xml.getRootElement().getName();
		if (type.equals(Q.DESIGN_SPACE_ROOT)) {
			exportDesignSpace(xml);
		} else if (type.equals(Q.DESIGN_ROOT)) {
			exportDesign(xml);
		} else if (type.equals(Q.MAPPINGS)) {
			exportCodeMappings(xml);
		} else
			throw new IllegalArgumentException(
					"The document you supplied is not of InPUT type because the root element has the name '"
							+ type + "', which is not supported.");
		return null;
	}

	private void exportCodeMappings(Document xml) {
		throw new IllegalArgumentException(
				"The method is not supported for code mappings (yet).");

	}

	@SuppressWarnings("unchecked")
	private void exportDesign(Document xml) throws InPUTException {
		String id = xml.getRootElement().getAttributeValue(Q.ID_ATTR);

		StringBuilder b = new StringBuilder();
		appendDesignTableStart(b, id);

		List<Element> values = xml.getRootElement().getChildren();
		for (Element value : values) {
			b.append(linebreak);
			b.append("\\hline");
			b.append(linebreak);
			appendValueToTable(b, value);
		}
		appendDesignTableEnd(b, id);

		writeFile(b);
	}

	private void appendDesignTableEnd(StringBuilder b, String id) {
		b.append("\\end{tabular}");
		b.append(linebreak);
		b.append("\\caption{The parameter values for design \\textit{");
		b.append(id);
		b.append("}.}");
		b.append(linebreak);
		b.append("\\label{table:design-");
		b.append(id);
		b.append("}");
		b.append(linebreak);
		b.append("\\end{table}");
	}

	private void appendValueToTable(StringBuilder b, Element value) {
		b.append(value.getAttributeValue(Q.ID_ATTR));
		b.append(" & ");
		extractAndAppendValue(b, value);
		b.append("\\\\");
		b.append(linebreak);
		b.append("\\hline");
		b.append(linebreak);
	}

	@SuppressWarnings("unchecked")
	private void extractAndAppendValue(StringBuilder b, Element value) {
		if (value.getAttribute(Q.VALUE_ATTR) != null) {
			if (value.getName().equals(Q.NVALUE)) {
				b.append("$");
				b.append(value.getAttributeValue(Q.VALUE_ATTR));
				b.append("$");
			} else if (value.getName().equals(Q.SVALUE)) {
				appendStructuralValue(b, value);
			}
		} else {
			List<Element> children = value.getChildren();
			int depth = getDepth(value);
			if (depth <= 2) {
				b.append("$\\left(");
				b.append(linebreak);
				b.append("\\begin{array}{");
				appendCs(b, depth);
				b.append("}");
				b.append(linebreak);
				appendMatrixContent(b, children, depth);
				b.append("\\end{array}");
				b.append(linebreak);
				b.append("\\right)$");
			} else {
				b.append("Too high dimensional to visualize.");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void appendStructuralValue(StringBuilder b, Element value) {
		b.append(value.getAttributeValue(Q.VALUE_ATTR));
		List<Element> children = value.getChildren();
		if (!children.isEmpty()) {
			int count = 0;
			b.append("(");
			for (Element child : children) {
				b.append(child.getAttributeValue(Q.ID_ATTR));
				b.append("=");
				extractAndAppendValue(b, child);
				if (count < children.size() - 1)
					b.append(", ");
				count++;
			}
			b.append(")");
		}
	}

	private void appendMatrixContent(StringBuilder b, List<Element> children,
			int depth) {
		int count = 0;
		for (Element child : children) {
			appendMatrixRow(b, child, depth);
			if (count < children.size() - 1)
				b.append("\\\\");
			count++;
		}
	}

	@SuppressWarnings("unchecked")
	private void appendMatrixRow(StringBuilder b, Element child, int depth) {
		if (child.getAttributeValue(Q.VALUE_ATTR) == null)
			appendMatrixRow(b, (Element) child.getChildren().get(0), depth);
		else {
			if (depth == 1)
				extractAndAppendValue(b, child);
			else {
				List<Element> values = child.getParentElement().getChildren();
				for (int i = 0; i < values.size(); i++) {
					extractAndAppendValue(b, values.get(i));
					if (i < values.size() - 1)
						b.append("&");
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
			child = (Element) child.getChildren().get(0);
		}
		return count;
	}

	private void appendCs(StringBuilder b, int depth) {
		for (int i = 0; i < depth; i++)
			b.append("c");
	}

	private void appendDesignTableStart(StringBuilder b, String id) {
		b.append("\\begin{table}");
		b.append(linebreak);
		b.append("\\centering");
		b.append(linebreak);
		b.append("\\begin{tabular}{|l|l|}");
		b.append(linebreak);
		b.append("\\hline");
		b.append(linebreak);
		b.append("\\textbf{Parameter} & \\textbf{Value} \\\\");
		b.append(linebreak);
		b.append("\\hline");
		b.append(linebreak);
	}

	@SuppressWarnings("unchecked")
	private void exportDesignSpace(Document xml) throws InPUTException {
		String id = xml.getRootElement().getAttributeValue(Q.ID_ATTR);

		StringBuilder b = new StringBuilder();
		appendDesignSpaceTableStart(b, id);

		List<Element> params = xml.getRootElement().getChildren();
		for (Element param : params) {
			b.append(linebreak);
			b.append("\\hline");
			b.append(linebreak);
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
			throw new InPUTException("The file by name '" + fileName
					+ "' could not be written successfully.", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void appendParamToTable(StringBuilder b, Element param, String id)
			throws InPUTException {

		if (!param.getName().equals(Q.SCHOICE)) {
			appendParamId(b, param);
			b.append(" & \\texttt{");
			b.append(getTypeString(param));
			b.append("} & ");
			b.append(getRangeString(param));
			b.append("\\\\");
			b.append(linebreak);
			b.append("\\hline");
			b.append(linebreak);
		}

		List<Element> children = param.getChildren();
		for (Element child : children) {
			if (!child.getName().equals(Q.SCHOICE)) {
				b.append("\\hspace{2 mm}");
			}
			appendParamToTable(b, child, id);
		}
	}

	private void appendParamId(StringBuilder b, Element param) {
		String paramId = param.getAttributeValue(Q.ID_ATTR);
		if (param.getParentElement().getName().equals(Q.SCHOICE)) {
			b.append("\\textit{");
			b.append(param.getParentElement().getAttributeValue(
					Q.ID_ATTR));
			b.append("}.");
		} else {
			b.append(paramId);
		}
	}

	private Object getRangeString(Element param) throws InPUTException {
		String value = null;
		if (param.getName().equals(Q.SPARAM))
			value = getStructRangeString(param);
		else if (param.getName().equals(Q.NPARAM))
			value = getNumericalRange(param);
		else {
			throw new InPUTException(
					"The given parameter element cannot be recognized as such by InPUT.");
		}
		return value;
	}

	private String getNumericalRange(Element original) throws InPUTException {
		Element clone = (Element) original.clone();
		new Element("something").addContent(clone);
		NParam element = new NParam(clone,
				"something", null);
		element.addMaxDependency(element);
		element.addMinDependency(element);
		return "$" + new Ranges(element).toString() + "$";
	}

	private static String getStructRangeString(Element param) {
		StringBuilder b = new StringBuilder("\\{");
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
		return b.toString();
	}

	private String getTypeString(Element param) throws InPUTException {
		String value = null;
		if (param.getName().equals(Q.SPARAM))
			value = "structured";
		else if (param.getName().equals(Q.NPARAM))
			value = param.getAttributeValue(Q.TYPE_ATTR);
		else {
			throw new InPUTException(
					"The given parameter element can not be recognized as such by InPUT.");
		}
		return value;
	}

	private void appendDesignSpaceTableEnd(StringBuilder b, String id) {
		b.append("\\end{tabular}");
		b.append(linebreak);
		b.append("\\caption{The parameter ranges for design space \\textit{");
		b.append(id);
		b.append("}.}");
		b.append(linebreak);
		b.append("\\label{table:paramranges-");
		b.append(id);
		b.append("}");
		b.append(linebreak);
		b.append("\\label{table:design-space-");
		b.append(id);
		b.append("}");
		b.append(linebreak);
		b.append("\\end{table}");
	}

	private void appendDesignSpaceTableStart(StringBuilder b, String id) {
		b.append("\\begin{table}");
		b.append(linebreak);
		b.append("\\centering");
		b.append(linebreak);
		b.append("\\begin{tabular}{|l|l|c|}");
		b.append(linebreak);
		b.append("\\hline");
		b.append(linebreak);
		b.append("\\textbf{Parameter} & \\textbf{Type} & \\textbf{Range} \\\\");
	}

	@Override
	public Void export(InPUTExportable input) throws InPUTException {
		throw new IllegalArgumentException(
				"The method is not supported for a complete InPUT object (yet).");
	}
}