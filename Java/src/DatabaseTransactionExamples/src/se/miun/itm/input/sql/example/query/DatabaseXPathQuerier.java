package se.miun.itm.input.sql.example.query;

import java.io.File;

import se.miun.itm.input.sql.example.DatabaseConnectingDialog;

/**
 * An example that demonstrates how SQL statements containing XPath
 * expressions can be used to query databases with the InPUT SQL
 * schema about the contents of designs and design spaces.
 * 
 * <p><b>Note</b>: This example has only been tested with the DBMS
 * <i>PostgreSQL 9.3</i>, and may therefore not work with other
 * systems/versions.</p>
 * 
 * @author Stefan Karlsson
 */
public class DatabaseXPathQuerier {
	
	public static void main(String[] args) throws Exception {
		new SQLFileDatabaseQuerier().query(DatabaseConnectingDialog.connect(),
			new File(System.getProperty("user.dir") + File.separator +
				"sql" + File.separator + "xpath"));
	}
}
