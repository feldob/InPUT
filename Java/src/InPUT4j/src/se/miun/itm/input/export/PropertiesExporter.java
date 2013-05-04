/*-- $Copyright (C) 2012-13 Felix Dobslaw$

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
 */package se.miun.itm.input.export;

import java.util.Properties;

import org.jdom2.Element;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.Param;
import se.miun.itm.input.model.param.ParamStore;
import se.miun.itm.input.model.param.SChoice;
import se.miun.itm.input.util.ParamUtil;
import se.miun.itm.input.util.Q;

/**
 * 
 * @author Felix Dobslaw
 * 
 * @ThreadSafe
 *
 */
public class PropertiesExporter implements InPUTExporter<Properties> {

	@Override
	public Properties export(Document xml) throws InPUTException {
		Properties prop = new InputProperties();
		initProperties(prop, xml.getRootElement());
		return prop;
	}

	private void initProperties(Properties prop, Element parent) {
		String spaceID = parent.getDocument().getRootElement().getAttributeValue(Q.REF_ATTR);
		ParamStore store = ParamStore.getInstance(spaceID); 
		initProperties(prop,store, parent);
	}

	private void initProperties(Properties prop, ParamStore store, Element parent) {
		String paramId, value;
		for (Element child : parent.getChildren()) {
			paramId = ParamUtil.deriveInputParamId(store, child);
			value = getValue(child);
			if (value != null && !(child instanceof SChoice))
				prop.put(paramId, value);
			initProperties(prop,store, child);
		}
	}

	private String getValue(Element child) {
		if (child instanceof Param<?>)
			return child.toString();
		else
			// assume that (child instanceof Value<?>)
			return child.getAttributeValue(Q.VALUE_ATTR);
	}

	@Override
	public Properties export(Exportable input) throws InPUTException {
		throw new InPUTException("IInPUT and IExperiment is not supported yet.");
	}

	@Override
	public String getInfo() {
		return "Properties exporter for InPUT.";
	}
}
