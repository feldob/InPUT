package se.miun.itm.input.export;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Properties;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.design.IDesign;

/**
 * 
 * @author Felix Dobslaw
 * 
 * @ThreadSafe
 *
 */
public class ExportHelper {

	private static void appendExportableToBuilder(StringBuilder b, String type,
			Exportable exportable) {
		if (exportable == null)
			return;
			
		b.append(type);
		b.append(":\n");
		b.append(exportable.toString());
		b.append('\n');
	}

	public static String exportableToString(Exportable exportable) {
		if (exportable instanceof IExperiment) {
			return exportExperimentExportable((IExperiment)exportable);
		}else if(exportable instanceof IInPUT){
			return exportIExportable((IInPUT)exportable);
		}else{
			return exportDesignExportable(exportable);
		}
	}

	private static String exportIExportable(IInPUT input) {
		StringBuilder b = new StringBuilder();
		ExportHelper.appendExportableToBuilder(b, "algorithm space", input.getAlgorithmDesignSpace());
		ExportHelper.appendExportableToBuilder(b, "property space", input.getPropertySpace());
		ExportHelper.appendExportableToBuilder(b, "problem feature space", input.getProblemFeatureSpace());
		ExportHelper.appendExportableToBuilder(b, "output space", input.getOutputSpace());
		return b.toString();
	}

	private static String exportDesignExportable(Exportable exportable) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Properties prop;
		try {
			prop = exportable.export(new PropertiesExporter());
			prop.store(out, null);
		} catch (Exception e) {
			// this should not effect anything if it does not work
		}
		return out.toString();
	}

	private static String exportExperimentExportable(IExperiment exp) {
		StringBuilder b = new StringBuilder();
		ExportHelper.appendExportableToBuilder(b, "algorithm design", exp.getAlgorithmDesign());
		ExportHelper.appendExportableToBuilder(b, "preferences", exp.getPreferences());
		ExportHelper.appendExportableToBuilder(b, "problem features", exp.getProblemFeatures());
		
		appendOutputs(b, exp.getOutput());
		return b.toString();
	}
	

	private static void appendOutputs(StringBuilder b, List<IDesign> outputs) {
		if (outputs.size() == 0)
			return;
		
		b.append("Results:\n");
		for (int i = 0; i < outputs.size(); i++) {
			ExportHelper.appendExportableToBuilder(b, "result "+ (i+1), outputs.get(i));
		}
	}
}
