package se.miun.itm.input.util.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.sql.Table.ColumnMetaDatum;

/**
 * Used for creating the InPUT SQL schema in databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseInitializer extends AbstractDatabaseConnector {
	
	/**
	 * A thread-safe cache of connections to databases that have the InPUT SQL schema.
	 */
	private static Set<Connection> initializedDatabases =
		Collections.synchronizedSet(Collections.newSetFromMap(
			new WeakHashMap<Connection, Boolean>())); 
	
	/**
	 * Maps names of SQL data types that are unsupported
	 * by the connected DBMS into supported alternatives.
	 */
	private Map<String, String> unsupportedTypesReplacements;
	
	/**
	 * Constructs a {@code DatabaseInitializer} with the given database connection.
	 * 
	 * @param conn A database connection.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseInitializer(Connection conn) throws SQLInPUTException {
		setConnection(conn);
	}
	
	/**
	 * Checks whether the connected database has the InPUT SQL schema.
	 * 
	 * @return	{@code true} if the database has the InPUT SQL schema,
	 * 			otherwise {@code false}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	@SuppressWarnings("resource") // the warning can safely be ignored
	public boolean databaseIsInitialized() throws SQLInPUTException {
		DatabaseMetaData metaData;
		ResultSet rs = null;

		if (initializedDatabases.contains(conn))
			return true;
		
		try {
			metaData = conn.getMetaData();
			
			for (Table table : Q.SQL_TABLES) {
				rs = metaData.getColumns(null, Q.SQL_SCHEMA_NAME, table.NAME, null);

				if (rs == null)
					return false;
				
				if (!checkColumnMetaData(rs, getSupportedAlternative(table).COLUMN_META_DATA))
					return false;
			}
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(rs);
		}

		initializedDatabases.add(conn);
		
		return true;
	}
	
	/**
	 * Creates the InPUT SQL schema in the connected database.
	 * 
	 * @throws SQLInPUTException If a database error occurs.
	 */
	@SuppressWarnings({ "unchecked", "resource" }) // the warnings can safely be ignored
	public void initDatabase() throws SQLInPUTException {
		boolean initializationComplete = false;
		Boolean oldAutoCommit = null;
		Savepoint sp1 = null;
		Statement stmt = null;
 
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp1 = conn.setSavepoint();

			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE SCHEMA " + Q.SQL_SCHEMA_NAME);
			
			for (Table table : Q.SQL_TABLES)
				stmt.executeUpdate(getSupportedAlternative(table).DEFINITION);
				
			for (Set<String> statements : Arrays.asList(
					Q.SQL_INDEXES, Q.SQL_FUNCTIONS, Q.SQL_DATA)) {
				for (String s : statements) {
					Savepoint sp2 = conn.setSavepoint();
					
					try {
						stmt.executeUpdate(s);
					} catch (SQLException e) {

						// the indexes and functions are not strictly required
						if (Q.SQL_DATA.contains(s))
							throw e;
						else
							conn.rollback(sp2);
					}
				}
			}
			
			if (oldAutoCommit)
				conn.commit();

			initializationComplete = true;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!initializationComplete)
				DatabaseAdapter.rollback(conn, sp1);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
			DatabaseAdapter.close(stmt);
		}
	}
	
	@Override
	public void setConnection(Connection newConn) throws SQLInPUTException {
		Map<String, String> unsupportedTypesReplacements =
			new LinkedHashMap<String, String>();
		ResultSet rs = null;
		Set<String>	types = new HashSet<String>();
		
		try {
			DatabaseMetaData metaData = newConn.getMetaData();
			rs = metaData.getTypeInfo();
			
			while (rs.next())
				types.add(rs.getString("TYPE_NAME").toUpperCase(Locale.ENGLISH));
	
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(rs);
		}
		
		if (!types.contains("BLOB")) {
			
			if (types.contains("BYTEA"))
				unsupportedTypesReplacements.put("BLOB", "BYTEA");
			else if (types.contains("VARBINARY"))
				unsupportedTypesReplacements.put("BLOB", "VARBINARY(8000)");
		}
		
		super.setConnection(newConn);
		this.unsupportedTypesReplacements = unsupportedTypesReplacements;
	}
	
	/**
	 * Checks whether the given {@link ResultSet} corresponds to the given
	 * column metadata. The {@code ResultSet} should be retrieved from the
	 * {@code getColumns} method of a {@link DatabaseMetaData} object, and
	 * its cursor should be positioned before the first row.
	 * 
	 * @param rs	The {@code ResultSet} that shall be checked to see if it 
	 * 				corresponds to the column metadata.
	 * @param metaData	The column metadata that shall be checked to see if
	 * 					it corresponds to the {@code ResultSet}.
	 * @return	{@code true} if the {@code ResultSet} corresponds to the
	 * 			column metadata, otherwise {@code false}.
	 * @throws SQLException If a database error occurs.
	 */
	private boolean checkColumnMetaData(ResultSet rs, List<ColumnMetaDatum> metaData) throws SQLException {
		int columnCount, ordinalPosition;
		ColumnMetaDatum metaDatum;
		
		for (columnCount = 0; rs.next(); columnCount++) {
			ordinalPosition = rs.getInt("ORDINAL_POSITION");
			
			if (ordinalPosition > metaData.size())
				return false;
			
			metaDatum = metaData.get(ordinalPosition - 1);
			
			if (!rs.getString("COLUMN_NAME").equalsIgnoreCase(metaDatum.COLUMN_NAME) ||
					rs.getInt("DATA_TYPE") != metaDatum.DATA_TYPE ||
					rs.getInt("NULLABLE") != metaDatum.NULLABLE ||
					metaDatum.CHAR_OCTET_LENGTH != null &&
						rs.getInt("CHAR_OCTET_LENGTH") != metaDatum.CHAR_OCTET_LENGTH)
				return false;
		}
		
		if (columnCount != metaData.size())
			return false;
		
		return true;
	}
	
	/**
	 * Returns a table whose definition is the same as that of the given table except that
	 * unsupported data types are replaced with supported alternatives.
	 * 
	 * @param table The table whose definition shall have its unsupported data types replaced.
	 * @return	A table whose definition is the same as the given one except that unsupported
	 * 			data types are replaced with alternatives supported by the connected DBMS.
	 * @throws SQLException If a database error occurs.
	 */
	private Table getSupportedAlternative(Table table) throws SQLException {
		String newDef = table.DEFINITION;
		
		// Replaces all the unsupported data types of the table.
		for (Map.Entry<String, String> entry : unsupportedTypesReplacements.entrySet())
			newDef = newDef.replaceAll("(?i)" + entry.getKey(), entry.getValue());
		
		try {
			return new Table(newDef);
		} catch (InPUTException e) {
			// This code should be unreachable.
			e.printStackTrace();
			System.exit(1);
		}
		
		throw new InternalError("Reached supposedly unreachable code.");
	}
}
