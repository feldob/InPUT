package se.miun.itm.input.sql.example;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Locale;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.impOrt.InPUTArchiveImporter;
import se.miun.itm.input.model.IOInPUTException;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * Used for transferring {@link IExperiment}:s from archive files to SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class ExperimentArchivesToDatabaseTransferrer extends FilesToDatabaseTransferrer<IExperiment> {
	
	/**
	 * Used for importing experiments from archive files.
	 */
	private ExperimentArchiveImporter importer = new ExperimentArchiveImporter();
	
	/**
	 * The {@code IInPUT} of the experiments.
	 */
	private IInPUT inPUT;
	
	/**
	 * Transfers the experiments contained in the archive files in the directory
	 * "<<i>current user directory</i>>/wsn-data/experiments" to a SQL
	 * database that the user chooses via a {@link DatabaseConnectingDialog}.
	 * 
	 * @param args Isn't used.
	 * @throws Exception If an error occurs.
	 */
	public static void main(String[] args) throws Exception {
		String wsnPathname = System.getProperty("user.dir") +
			File.separator + "wsn-data";

		new ExperimentArchivesToDatabaseTransferrer(new InPUTArchiveImporter("wsn-ea",
			wsnPathname + File.separator + "wsn-ea.inp").impOrt()).transferFiles(new File(
				wsnPathname + File.separator + "experiments"), DatabaseConnectingDialog.connect());
	}
	
	/**
	 * Constructs a {@code ExperimentArchivesToDatabaseTransferrer}.
	 * 
	 * @param inPUT	 The {@code IInPUT} of the experiments that shall exported.
	 */
	public ExperimentArchivesToDatabaseTransferrer(IInPUT inPUT) {
		super(new FileFilter() {
			public boolean accept(File f) {
				return	f.canRead() &&
						f.getName().toLowerCase(Locale.ENGLISH).endsWith(".exp");
			}
		},
		"Transferring the experiments from the archive files to the database...");
		this.inPUT = inPUT;
	}

	@Override
	protected void export(IExperiment experiment, DatabaseAdapter adapter) throws InPUTException, SQLInPUTException {
		
		if (!adapter.export(experiment))
			adapter.update(experiment);
	}

	@Override
	protected IExperiment impOrt(File f) throws InPUTException, IOInPUTException, SQLInPUTException {
		IExperiment experiment;
		String filename = f.getName();

		importer.resetFileName(f.getAbsolutePath());
		experiment = new Experiment(inPUT.getId() + "-experiment-" +
			filename.substring(0, filename.indexOf('.')), inPUT);
		
		try {
			experiment.impOrt(importer);
		} catch (IOException e) {
			throw new IOInPUTException(e.getMessage(), e);
		}
		
		return experiment;
	}
}
