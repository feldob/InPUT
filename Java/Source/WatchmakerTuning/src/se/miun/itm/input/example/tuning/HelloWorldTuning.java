package se.miun.itm.input.example.tuning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.export.ZipFileExporter;
import se.miun.itm.input.impOrt.InPUTArchiveImporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.tuning.sequential.SPOT;
import se.miun.itm.input.tuning.sequential.spot.SPOTQ;

public class HelloWorldTuning {
	
	protected static final String STRING_IDENTIFICATION_ID = "StringIdentification";

	protected static final InPUTArchiveImporter IMPORTER = new InPUTArchiveImporter(STRING_IDENTIFICATION_ID, STRING_IDENTIFICATION_ID + ".inp");

	protected final static ZipFileExporter EXPORTER = new ZipFileExporter();
	
	private final IInPUT stringIdentification;
	
	private final SPOT tuner; 
	
	public static void main(String[] args) throws InPUTException {
		new HelloWorldTuning().tune();
	}
	
	private void tune() throws InPUTException {
		HelloWorldExecutor executor = new HelloWorldExecutor();
		
		IExperiment experiment;
		IDesign result;
		String folder = tuner.getExperimentalFolderPath();
		for (int i = 0; i < 1000; i++) {
			experiment = tuner.nextExperiment();
			result = executor.execute(experiment);
			System.out.println("experiment " + (i+1)+".");
			tuner.feedback(result);
			EXPORTER.resetFileName(folder + File.separator + experiment.getId());
			experiment.export(EXPORTER);
		}
	}

	public HelloWorldTuning() throws InPUTException {
		stringIdentification = InPUT.getInPUT(IMPORTER);
		tuner = new SPOT(stringIdentification, readProblem(), "spotConfig.xml", "HelloWorldFinder", true);
	}
	
	private List<IDesign> readProblem() throws InPUTException {
		List<IDesign> problems = new ArrayList<IDesign>();
		problems.add(new Design("problemFeatures.xml"));
		return problems;
	}
}
