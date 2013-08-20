package se.miun.itm.input.util.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.sql.Table.ColumnMetaDatum;
import se.miun.itm.input.util.sql.Table.Row;

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
	 * A placeholder for the name of a SQL sequence.
	 */
	private final String SEQUENCE_NAME_PLACEHOLDER = "sequence_name_placeholder"; 
	
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
	public boolean databaseIsInitialized() throws SQLInPUTException {
		DatabaseMetaData metaData;
		PreparedStatement retrieveData = null;

		if (initializedDatabases.contains(conn))
			return true;
		
		try {
			ResultSet rs;
			
			metaData = conn.getMetaData();
			
			for (Table table : Q.SQL_TABLES) {
				List<Table.Row> rows;
				
				rs = metaData.getColumns(null, Q.SQL_SCHEMA_NAME, table.NAME, null);

				if (rs == null)
					return false;
				
				// Checks whether the current table has the required definition.
				if (!checkColumnMetaData(rs, getSupportedAlternative(table).COLUMN_META_DATA)) {
					rs.close();
					return false;
				}

				rs.close();
				
				rows = table.ROWS;
				
				// Checks whether the current table has the required rows.
				if (rows.size() > 0) {
					Table.Row firstRow = rows.get(0);
					StringBuilder statementBuilder = new StringBuilder(
						"SELECT * FROM " + Q.SQL_SCHEMA_NAME + '.' + table.NAME +
						" WHERE ");
					
					statementBuilder.append(firstRow.get(0).COLUMN_NAME + " = ?");
					
					for (int i = 1; i < firstRow.size(); i++)
						statementBuilder.append(
							" AND " + firstRow.get(i).COLUMN_NAME + " = ?");
					
					retrieveData = conn.prepareStatement(statementBuilder.toString());
					
					for (Table.Row r : rows) {
						
						for (int i = 0; i < r.size(); i++)
							r.get(i).setPreparedStatementParameter(retrieveData, i + 1);

						rs = retrieveData.executeQuery();
						
						if (!rs.next())
							return false;
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(retrieveData);
		}

		initializedDatabases.add(conn);
		
		return true;
	}
	
	/**
	 * Creates the InPUT SQL schema in the connected database.
	 * 
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public void initDatabase() throws SQLInPUTException {
		boolean initializationComplete = false;
		Boolean oldAutoCommit = null;
		PreparedStatement insertData = null;
		Savepoint sp = null;
		Statement stmt = null;
 
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();

			stmt = conn.createStatement();
			stmt.executeUpdate("CREATE SCHEMA " + Q.SQL_SCHEMA_NAME);
			
			for (Table table : Q.SQL_TABLES) {
				List<Table.Row> rows = table.ROWS;
				
				// Creates the SQL table represented by the current 'Table' object.
				stmt.executeUpdate(getSupportedAlternative(table).DEFINITION);
				
				// Creates the indexes of the newly created SQL table.
				for (String index : table.INDEXES)
					stmt.executeUpdate(index);
				
				/* Inserts the rows of the current 'Table'
				 * object into the newly created SQL table. */
				if (rows.size() > 0) {
					StringBuilder statementBuilder = new StringBuilder(
						"INSERT INTO " + Q.SQL_SCHEMA_NAME + '.' + table.NAME +
						" VALUES (?");
					
					for (int i = 1; i < rows.get(0).size(); i++)
						statementBuilder.append(", ?");
					
					statementBuilder.append(')');
					insertData = conn.prepareStatement(statementBuilder.toString());

					for (Table.Row r : rows) {
						
						for (int i = 0; i < r.size(); i++)
							r.get(i).setPreparedStatementParameter(insertData, i + 1);

						insertData.executeUpdate();
					}
				}
			}
					
			for (String function : Q.SQL_FUNCTIONS.get(conn.getMetaData().getDatabaseProductName()))
				stmt.executeUpdate(function);
			
			if (oldAutoCommit)
				conn.commit();

			initializationComplete = true;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!initializationComplete)
				DatabaseAdapter.rollback(conn, sp);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
			DatabaseAdapter.close(stmt, insertData);
		}
	}
	
	@Override
	public void setConnection(Connection newConn) throws SQLInPUTException {
		DatabaseMetaData metaData;
		Map<String, String> unsupportedTypesReplacements =
			new LinkedHashMap<String, String>();
		ResultSet rs = null;
		Set<String>	types = new HashSet<String>();
		String replacement = null;
		
		try {
			metaData = newConn.getMetaData();
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
			
		if (types.contains("AUTOINCREMENT"))
			replacement = "AUTOINCREMENT";
		else if (types.contains("AUTO_INCREMENT"))
			replacement = "int AUTO_INCREMENT";
		else if (types.contains("AUTONUMBER"))
			replacement = "AUTONUMBER";
		else if (types.contains("IDENTITY"))
			replacement = "int IDENTITY";
		else if (types.contains("SERIAL"))
			replacement = "SERIAL";
		else  {
			Boolean oldAutoCommit = null;
			Statement stmt = null;
			String sequenceName = UUID.randomUUID().toString().replace('-', '_');
			
			try {
				oldAutoCommit = newConn.getAutoCommit();
				newConn.setAutoCommit(false);
				
				stmt = newConn.createStatement();
				stmt.executeUpdate("CREATE SEQUENCE " + sequenceName);
				newConn.commit();
				
				try {
					rs = stmt.executeQuery("SELECT NEXT VALUE FOR " + sequenceName);
					
					if (rs.next() && rs.getString(1) != null)
						replacement = "DEFAULT NEXT VALUE FOR " + SEQUENCE_NAME_PLACEHOLDER;
					
				} catch (SQLException e) {
					newConn.rollback();
				}
				
				try {
					rs = stmt.executeQuery("SELECT " + sequenceName + ".nextval");
					
					if (rs.next() && rs.getString(1) != null)
						replacement = "DEFAULT " + SEQUENCE_NAME_PLACEHOLDER + ".nextval";
					
				} catch (SQLException e) {
					newConn.rollback();
				}
				
				stmt.executeUpdate("DROP SEQUENCE " + sequenceName);
				newConn.commit();
			} catch (SQLException e1) {
				try {
					newConn.rollback();
				} catch (SQLException e2) {
					throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e2), e2);
				}
			} finally {
				DatabaseAdapter.setAutoCommit(newConn, oldAutoCommit);
				DatabaseAdapter.close(stmt);
			}
		}
			
		if (replacement == null)
			replacement = "int DEFAULT 1";
		
		unsupportedTypesReplacements.put(Q.SQL_AUTO_INCREMENT_PLACEHOLDER, replacement);
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
		int columnCount, ordinalPos;
		ColumnMetaDatum metaDatum;
		
		for (columnCount = 0; rs.next(); columnCount++) {
			ordinalPos = rs.getInt("ORDINAL_POSITION");
			
			if (ordinalPos > metaData.size())
				return false;
			
			metaDatum = metaData.get(ordinalPos - 1);
			
			if (!rs.getString("COLUMN_NAME").equalsIgnoreCase(metaDatum.COLUMN_NAME) ||
				rs.getInt("DATA_TYPE") != metaDatum.DATA_TYPE ||
				rs.getInt("NULLABLE") != metaDatum.NULLABLE ||
				metaDatum.CHAR_OCTET_LENGTH != null &&
					rs.getInt("CHAR_OCTET_LENGTH") != metaDatum.CHAR_OCTET_LENGTH)
			{
				return false;
			}
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
		String newDef = new String(table.DEFINITION);
		
		// Replaces all the unsupported data types of the table.
		for (Map.Entry<String, String> entry : unsupportedTypesReplacements.entrySet())
			newDef = newDef.replaceAll("(?i)" + entry.getKey(), entry.getValue());
		
		if (newDef.contains(SEQUENCE_NAME_PLACEHOLDER)) {
			Statement stmt = conn.createStatement();
			String sequenceName = table.NAME + "_sequence";
			
			newDef = newDef.replaceAll(SEQUENCE_NAME_PLACEHOLDER, sequenceName);
			stmt.executeUpdate("CREATE SEQUENCE " + sequenceName);
			stmt.close();
		}
		
		try {
			return new Table(newDef,
					table.INDEXES.toArray(new String[table.INDEXES.size()]),
					table.ROWS.toArray(new Row[table.ROWS.size()]));
		} catch (InPUTException e) {
			// This code should be unreachable.
			e.printStackTrace();
			System.exit(1);
		}
		
		// Unreachable code.
		throw new InternalError();
	}
}
