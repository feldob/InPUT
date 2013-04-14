package se.miun.itm.input.injection;

import se.miun.itm.input.annotation.Output;
import se.miun.itm.input.export.XMLArchiveExporter;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.design.IDesign;

public aspect OtherInjection {

	after() : Injection.initOutputDesignMethod() || Injection.initOutputDesignConstructor(){
		Output outputAnn = (Output) Injection.getAnnotation(Output.class,
				thisJoinPointStaticPart);

		IDesign design = Injection.designs.get(outputAnn.id());
		try {
			design.attachEnvironmentInfo();
			design.export(new XMLArchiveExporter(outputAnn.file()));
		} catch (InPUTException e) {
			// TODO remove for production
			e.printStackTrace();
		}
	}
}