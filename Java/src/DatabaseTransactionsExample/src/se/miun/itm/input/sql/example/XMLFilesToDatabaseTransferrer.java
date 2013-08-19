package se.miun.itm.input.sql.example;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import se.miun.itm.input.impOrt.XMLFileImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTDocument;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * Used for transferring {@link Document}:s from XML files to SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class XMLFilesToDatabaseTransferrer extends FilesToDatabaseTransferrer<InPUTDocument> {
	
	/**
	 * Used for importing documents from XML files.
	 */
	private XMLFileImporter importer = new XMLFileImporter();
	
	/**
	 * Transfers the documents contained in the XML files in
	 * the current user directory to a SQL database that the
	 * user  chooses via a {@link DatabaseConnectingDialog}.
	 * 
	 * @param args Isn't used.
	 * @throws Exception If an error occurs.
	 */
	public static void main(String[] args) throws Exception {
		new XMLFilesToDatabaseTransferrer().transferFiles(
			new File(System.getProperty("user.dir")),
			DatabaseConnectingDialog.connect());
	}
	
	/**
	 * Constructs a {@code XMLFilesToDatabaseTransferrer}.
	 */
	public XMLFilesToDatabaseTransferrer() {
		super(new FileFilter() {
			public boolean accept(File f) {
				return	f.canRead() &&
						f.getName().toLowerCase(Locale.ENGLISH).endsWith(".xml");
			}
		},
		"Transferring the documents from the XML files to the database...");
	}

	@Override
	protected void export(InPUTDocument doc, DatabaseAdapter adapter) throws InPUTException, SQLInPUTException {
		
		if (!doc.isMapping())
			if (!adapter.export(doc))
				adapter.update(doc);
	}

	@Override
	protected InPUTDocument impOrt(File f) throws InPUTException {
		importer.resetFileName(f.getAbsolutePath());
		return new InPUTDocument(importer.impOrt());
	}
}
