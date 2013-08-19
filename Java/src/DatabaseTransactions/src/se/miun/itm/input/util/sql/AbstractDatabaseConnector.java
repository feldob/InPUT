package se.miun.itm.input.util.sql;

import java.sql.Connection;

import se.miun.itm.input.aspects.DatabaseConnector;
import se.miun.itm.input.model.SQLInPUTException;

/**
 * A skeletal implementation of the interface {@link DatabaseConnector}.
 * 
 * @author Stefan Karlsson
 */
public abstract class AbstractDatabaseConnector implements DatabaseConnector {

	/**
	 * A database connection.
	 */
	protected Connection conn;
	
	/**
	 * Constructs an {@code AbstractDatabaseConnector} 
	 * with the given database connection.
	 * 
	 * @param conn The database connection that this
	 * 				{@code AbstractDatabaseConnector} 
	 * 				shall use.
	 * @throws SQLInPUTException	If a database
	 * 								error occurs.
	 */
	protected AbstractDatabaseConnector(Connection conn) throws SQLInPUTException {
		this.conn = conn;
		prepareStatements();
	}
	
	/**
	 * Constructs an {@code AbstractDatabaseConnector}
	 * without any database connection.
	 */
	AbstractDatabaseConnector() {}
	
	public Connection getConnection() {
		return conn;
	}
	
	public void setConnection(Connection newConn) throws SQLInPUTException {
		boolean preparationComplete = false;
		Connection oldConn = conn;
		
		conn = newConn;
		
		try {
			prepareStatements();
			preparationComplete = true;
		} finally {
			
			if (!preparationComplete)
				conn = oldConn;
		}
	}
	
	/**
	 * Initializes the {@link java.sql.PreparedStatement PreparedStatment}
	 * fields of this {@code AbstractDatabaseConnector}.
	 * 
	 * @throws SQLInPUTException If a database error occurs.
	 */
	protected void prepareStatements() throws SQLInPUTException {}
}
