package se.miun.itm.input.sql.example.query;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.SortedMap;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.export.DatabaseExporter;
import se.miun.itm.input.impOrt.ExperimentArchiveImporter;
import se.miun.itm.input.impOrt.InPUTArchiveImporter;
import se.miun.itm.input.sql.example.DatabaseConnectingDialog;
import se.miun.itm.input.util.sql.DatabaseAdapter;

/**
 * An example that demonstrates how the contents of designs and design
 * spaces can be updated in databases with the InPUT SQL schema.
 * 
 * <p><b>Note</b>: This example has only been tested with the DBMS
 * <i>PostgreSQL 9.3</i>, and may therefore not work with other
 * systems/versions.</p>
 * 
 * @author Stefan Karlsson
 */
public class DatabaseXMLUpdater {
	
	public static void main(String[] args) throws Exception {
		IExperiment experiment;
		Connection conn = DatabaseConnectingDialog.connect();
		SortedMap<String, String> queries;
		SQLFileDatabaseQuerier querier = new SQLFileDatabaseQuerier();
		String idPathname = System.getProperty("user.dir") +
			File.separator + "string-identification-data";
		String query, update;
		
		queries = querier.readSQLFiles(new File(System.getProperty("user.dir") +
			File.separator + "sql" + File.separator + "xml-updates"));
		query = queries.get("update1-confirmation-query.sql");
		
		experiment = new Experiment("hello-world", new InPUTArchiveImporter(
				"string-identification", idPathname + File.separator +
					"string-identification.inp").impOrt());
		experiment.impOrt(new ExperimentArchiveImporter(
			idPathname + File.separator + "hello-world.exp"));
		
		if (new DatabaseExporter(conn).export(experiment))
			update = queries.get("update1.sql");
		else {
			ResultSet rs;
			Statement stmt = null;
			
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
				rs.next();
				
				update = rs.getString("target_string").equals("HELLO WORLD") ?
					queries.get("update1.sql") : queries.get("reverse-update1.sql");
			} finally {
				DatabaseAdapter.close(stmt);
			}
		}
		
		querier.query(conn, query, update, query);
	}
}
