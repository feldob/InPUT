package se.miun.itm.input.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.Design;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.util.InputStreamWrapper;

public class ProblemTopologyUtil {

	public static void addProblemFile(IExperiment experiment) throws InPUTException, FileNotFoundException, IOException {
		File problemFile = new File((String) experiment.getValue("WSN.InstanceFile"));
		ByteArrayOutputStream problemInstance = InputStreamWrapper.init(new FileInputStream(problemFile));
		experiment.addContent(problemFile.getName(), problemInstance);
	}

	public static void resetExperimentalFileLocation(IExperiment experiment, String instanceFolder) throws InPUTException {
		String filePath = experiment.getValueToString("WSN.InstanceFile");
		String[] path = filePath.split(File.separator);
		String fileName = path[path.length-1];
		String instanceFile =   instanceFolder + fileName;
		experiment.setValue("WSN.InstanceFile", instanceFile);
	}

	public static List<IDesign> importProblemInstances(IInPUT wsnEa, String instanceFolder) throws InPUTException, IOException {
		List<IDesign> problems = new ArrayList<IDesign>();
		File problemFolder = new File(instanceFolder);

		IDesign topology;
		for (File file : problemFolder.listFiles()) {
			if (file.getName().endsWith(".xml")) {
				topology = new Design(file.getAbsolutePath());
//				wsn = new WSN(problemFolder.getAbsolutePath() + File.separator + fileName);
//				topology = featureSpace.nextEmptyDesign(fileName.substring(0, fileName.length() - 4));
//				topology.setValue("WSN", wsn);
				problems.add(topology);
			}
		}
		return problems;
	}
}