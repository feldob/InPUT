package se.miun.itm.input.sql.example.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import se.miun.itm.input.model.IOInPUTException;
import se.miun.itm.input.model.SQLInPUTException;

/**
 * Used for reading queries from SQL files and sending them to SQL databases.
 * 
 * @author Stefan Karlsson
 */
public class SQLFileDatabaseQuerier extends DatabaseQuerier {

	/**
	 * Reads queries from the SQL files in the given directory and sends
	 * them to the designated database.
	 * 
	 * @param conn A connection to the database that shall be queried.
	 * @param queryDir	The directory containing the SQL files with the
	 * 					queries.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public void query(Connection conn, File queryDir) throws IOInPUTException, SQLInPUTException {
		Collection<String> queries = readSQLFiles(queryDir).values();
		query(conn, queries.toArray(new String[queries.size()]));
	}
	
	/**
	 * Returns a {@code SortedMap} that maps the names of the SQL files in
	 * the given directory to the queries that they contain. The filenames
	 * are in alphabetical order.
	 * 
	 * @param queryDir	The directory containing the SQL files with the
	 * 					queries.
	 * @throws IOInPUTException If an I/O error occurs.
	 */
	public SortedMap<String, String> readSQLFiles(File dir) throws IOInPUTException {
		char[] charArray = new char[100];
		Collator coll = Collator.getInstance(Locale.ENGLISH);
		NavigableMap<String, String> queries;
		
		coll.setStrength(Collator.PRIMARY);
		queries = new TreeMap<String, String>(coll);
		
		for (File f : dir.listFiles(new FileFilter() {
				public boolean accept(File f) {
					return	f.canRead() &&
							f.getName().toLowerCase(Locale.ENGLISH).endsWith(".sql");
				}
			}))
		{
			int charCount = 0;
			
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "UTF-8"));
			
				try {
				
					while (reader.ready()) {
				
						if (charCount == charArray.length)
							charArray = Arrays.copyOf(charArray, charArray.length * 2);
				
						charCount += reader.read(charArray, charCount,
								charArray.length - charCount);
					}
			
					queries.put(f.getName(), new String(charArray, 0, charCount));
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				throw new IOInPUTException(e.getMessage(), e);
			}
		}
		
		return queries;
	}
}
