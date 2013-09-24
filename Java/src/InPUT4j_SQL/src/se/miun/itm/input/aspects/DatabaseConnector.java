package se.miun.itm.input.aspects;

import java.sql.Connection;

import se.miun.itm.input.model.SQLInPUTException;

/**
 * An interface for classes whose instances uses SQL database connections.
 * 
 * @author Stefan Karlsson
 */
public interface DatabaseConnector {

	/**
	 * Returns the database connection that
	 * this {@code DatabaseConnector} uses.
	 */
	public Connection getConnection();
	
	/**
	 * Changes the database connection that that
	 * this {@code DatabaseConnector} uses.
	 * 
	 * @param newConn	The new database connection
	 * 					that this
	 * 					{@code DatabaseConnector}
	 * 					shall use.
	 * @throws SQLInPUTException	If a database 
	 * 								error occurs.
	 */
	public void setConnection(Connection newConn) throws SQLInPUTException;
}
