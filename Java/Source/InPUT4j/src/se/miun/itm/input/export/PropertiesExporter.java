package se.miun.itm.input.export;

import java.util.Properties;

import org.jdom2.Element;

import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.param.Param;
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
public class PropertiesExporter implements Exporter<Properties> {

	@Override
	public Properties export(Document xml) throws InPUTException {
		Properties prop = new InputProperties();
		initProperties(prop, xml.getRootElement());
		return prop;
	}

	private void initProperties(Properties prop, Element parent) {
		String paramId, value;
		for (Element child : parent.getChildren()) {
			paramId = ParamUtil.deriveInputParamId(child);
			value = getValue(child);
			if (value != null && !(child instanceof SChoice))
				prop.put(paramId, value);
			initProperties(prop, child);
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
