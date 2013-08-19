package se.miun.itm.input.util.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.export.DatabaseExporter;
import se.miun.itm.input.impOrt.DatabaseImporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.IOInPUTException;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;

/**
 * Used for performing SQL database transactions such as export, import,
 * updating and deletion of {@link Document}:s and {@link Exportable}:s.
 * 
 * @author Stefan Karlsson
 */
// A facade of all InPUT classes that interact with SQL databases.
public class DatabaseAdapter extends AbstractDatabaseConnector {
	
	/**
	 * Used for deleting objects from databases.
	 */
	private DatabaseDeleter deleter;
	
	/**
	 * Used for exporting objects to databases.
	 */
	private DatabaseExporter exporter;
	
	/**
	 * Used for importing objects from databases.
	 */
	private DatabaseImporter<Exportable> importer;
	
	/**
	 * Used for updating objects in databases.
	 */
	private DatabaseUpdater updater;
	
	/**
	 * Used for synchronizing the operations of this {@code DatabaseAdapter}
	 * if thread-safety is desired.
	 */
	private Lock lock;
	
	/**
	 * Constructs a thread-<i>unsafe</i> {@code DatabaseAdapter}
	 * with the given database connection.
	 * 
	 * @param conn	The database connection that this
	 * 				{@code DatabaseUtil} shall use.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseAdapter(Connection conn) throws SQLInPUTException {
		this(conn, false);
	}
	
	/**
	 * Constructs a {@code DatabaseAdapter} with the given
	 * database connection and specified thread-safety.
	 * 
	 * @param conn	The database connection that this {@code DatabaseUtil}
	 * 				shall use.
	 * @param threadSafe	{@code true} if this {@code DatabaseAdapter}
	 * 						shall be thread-safe, otherwise {@code false}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseAdapter(Connection conn, boolean threadSafe) throws SQLInPUTException {
		DatabaseInitializer initializer = new DatabaseInitializer(conn);
		
		if (!initializer.databaseIsInitialized())
			initializer.initDatabase();

		exporter = new DatabaseExporter(conn);
		importer = new DatabaseImporter<Exportable>(conn);
		updater = new DatabaseUpdater(conn);
		deleter = new DatabaseDeleter(conn);
		
		lock = threadSafe ? new ReentrantLock() : new DummyLock();
	}
	
	/**
	 * Checks each given {@link AutoCloseable} to see whether it's {@code null} and,
	 * if it isn't, closes it.
	 * 
	 * 
	 * @param autoCloseables The {@code AutoCloseable}:s that shall be closed.
	 * @throws SQLInPUTException	If any of the {@code AutoCloseable}:s isn't
	 * 								{@code null} and cannot be closed.
	 */
	public static void close(AutoCloseable ... autoCloseables) throws SQLInPUTException {
		
		for (AutoCloseable ac : autoCloseables)
			if (ac != null)
				try {
					ac.close();
				} catch (SQLException e) {
					throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
				} catch (Exception e) {
					throw new SQLInPUTException(e.getMessage(), e);
				}
	}
	
	/**
	 * Concatenates the message of the given {@link SQLException} with
	 * those of all the {@code SQLException}:s that follows in the chain.
	 * 
	 * @param firstException The first {@code SQLException} in the chain.
	 * @return	A concatenation of the messages of all the
	 * 			{@code SQLException}:s in the chain.
	 */
	public static String concatSQLMessages(SQLException firstException) {
		StringBuilder concatenatedMessages = new StringBuilder(
			"SQL state " + firstException.getSQLState());
		
		for (SQLException e = firstException; e != null; e = e.getNextException())
			concatenatedMessages.append('\n' + e.getMessage());
		
		return concatenatedMessages.toString();
	}
	
	/**
	 * Checks whether the given {@link Savepoint} is {@code null} and,
	 * if it isn't, undoes all changes made in the current transaction
	 * of the given {@link Connection} after the savepoint was set.
	 * 
	 * @param sp	The savepoint that the current transaction of the
	 * 				given {@code Connection} shall be rolled back to.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public static void rollback(Connection conn, Savepoint sp) throws SQLInPUTException {
		
		if (sp != null)
			try {
				conn.rollback(sp);
			} catch (SQLException e) {
				throw new SQLInPUTException(concatSQLMessages(e), e);
			}
	}
	
	/**
	 * Checks whether the given {@link Boolean} is {@code null} and, if it isn't,
	 * sets the auto commit mode of the given {@link Connection} in accordance
	 * with the {@code Boolean}.
	 * 
	 * @param autoCommit	{@code true} if the given {@code Connection} shall be
	 * 						auto committing, {@code false} if it shall <i>not</i>
	 * 						be auto committing, or {@code null} if its auto commit 
	 * 						mode shall remain unchanged.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public static void setAutoCommit(Connection conn, Boolean autoCommit) throws SQLInPUTException {
		
		if (autoCommit != null)
			try {
				conn.setAutoCommit(autoCommit);
			} catch (SQLException e) {
				throw new SQLInPUTException(concatSQLMessages(e), e);
			}
	}
	
	/**
	 * Deletes the given InPUT XML document from the connected database.
	 * 
	 * @param doc	The InPUT XML document to be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the specified document was not in the database.
	 * @throws InPUTException If the document isn't an InPUT XML document.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public boolean delete(Document doc) throws InPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return deleter.delete(doc);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Deletes the given {@link Exportable} from the connected database.
	 * 
	 * @param exportable The {@code Exportable} to be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the given {@code Exportable} was not in the database.
	 * @throws InPUTException	If the {@code Exportable} isn't a design,
	 * 							design space, code-mapping object,
	 * 							experiment or {@link IInPUT}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public boolean delete(Exportable exportable) throws InPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return deleter.delete(exportable);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Exports the given InPUT XML document to the connected database.
	 * 
	 * @param doc The InPUT XML document to be exported.
	 * @return	{@code true} if the document was successfully exported,
	 * 			or {@code false} if a document with the same ID and type
	 * 			is already in the database.
	 * @throws InPUTException	If the document isn't a design or design
	 * 							space document, or if it's dependent on
	 * 							a document that cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public boolean export(Document doc) throws InPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return exporter.export(doc);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Exports the given {@link Exportable} to the connected database.
	 * 
	 * @param exportable The {@code Exportable} to be exported.
	 * @return	{@code true} if the {@code Exportable} was successfully
	 * 			exported, or {@code false} if an {@code Exportable} with
	 * 			the same ID and type is already in the database.
	 * @throws InPUTException	If the {@code Exportable} isn't a design,
	 * 							design space, experiment or {@link IInPUT},
	 * 							or if a non-database export error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public boolean export(Exportable exportable) throws InPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return exporter.export(exportable);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Returns the database connection that this {@code DatabaseAdapter} uses.
	 * 
	 * @throws IllegalStateException If this {@code DatabaseAdapter} is thread-safe.
	 */
	@Override
	public Connection getConnection() {
		
		if (!(lock instanceof DummyLock))
			throw new IllegalStateException(
				"The 'getConnection' method shouldn't be " +
				"called on a thread-safe 'DatabaseAdapter'.");
		
		return conn;
	}
	
	/**
	 * Imports the specified design from the connected database.
	 * 
	 * @param designID The ID of the design to be imported.
	 * @return	The specified design if it's in the database, otherwise {@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IDesign importDesign(String id) throws InPUTException, IOInPUTException, SQLInPUTException {
		return importDesign(id, null);
	}
	
	/**
	 * Imports the specified design from the connected database.
	 * 
	 * @param designID The ID of the design to be imported.
	 * @param frameworkID	The ID of the framework that the code mapping object of
	 * 						the design's design space belongs to, or {@code null}
	 * 						if it doesn't belong to any framework.
	 * @return	The specified design if it's in the database, otherwise {@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IDesign importDesign(String designID, String frameworkID) throws InPUTException, IOInPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return importer.importDesign(designID, frameworkID);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Imports the specified design space from the connected database.
	 * 
	 * @param spaceID The ID of the design space to be imported.
	 * @return The specified design space if it's in the database, otherwise 
	 * 			{@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IDesignSpace importDesignSpace(String id) throws InPUTException, IOInPUTException, SQLInPUTException {
		return importer.importDesignSpace(id, null);
	}
	
	/**
	 * Imports the specified design space from the connected database.
	 * 
	 * @param spaceID The ID of the design space to be imported.
	 * @param frameworkID	The ID of the framework that the code mapping object
	 * 						of the design space belongs to, or {@code null} if it
	 * 						doesn't belong to any framework.
	 * @return The specified design space if it's in the database, otherwise 
	 * 			{@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IDesignSpace importDesignSpace(String spaceID, String frameworkID) throws InPUTException, IOInPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return importer.importDesignSpace(spaceID, frameworkID);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Imports the specified experiment from the connected database.
	 * 
	 * @param experimentID The ID of the experiment to be imported.
	 * @return The specified experiment if it's in the database, otherwise
	 * 			{@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IExperiment importExperiment(String id) throws InPUTException, IOInPUTException, SQLInPUTException {
		return importExperiment(id, null);
	}
	
	/**
	 * Imports the specified experiment from the connected database.
	 * 
	 * @param experimentID The ID of the experiment to be imported.
	 * @param frameworkID	The ID of the framework that the code mapping object
	 * 						of the design spaces of the experiment's designs
	 * 						belongs to, or {@code null} if it doesn't belong to
	 * 						any framework.
	 * @return The specified experiment if it's in the database, otherwise
	 * 			{@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IExperiment importExperiment(String experimentID, String frameworkID) throws InPUTException, IOInPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return importer.importExperiment(experimentID, frameworkID);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Imports the specified {@link IInPUT} from the connected database.
	 * 
	 * @param inPUTID The ID of the {@code IInPUT} to be imported.
	 * @return The specified descriptor if it's in the database, otherwise
	 * 			{@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IInPUT importInPUT(String id) throws InPUTException, IOInPUTException, SQLInPUTException {
		return importInPUT(id, null);
	}
	
	/**
	 * Imports the specified {@link IInPUT} from the connected database.
	 * 
	 * @param inPUTID The ID of the {@code IInPUT} to be imported.
	 * @param frameworkID	The ID of the framework that the code mappings of
	 * 						the {@code IInPUT}'s design spaces belong to, or
	 * 						{@code null} if they don't belong to any framework.
	 * @return The specified descriptor if it's in the database, otherwise
	 * 			{@code null}.
	 * @throws InPUTException	If a non-I/O non-database error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public IInPUT importInPUT(String inPUTID, String frameworkID) throws InPUTException, IOInPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return importer.importInPUT(inPUTID, frameworkID);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Returns {@code true} if this {@code DatabaseAdapter} is thread-safe,
	 * otherwise {@code false}.
	 */
	public boolean isThreadSafe() {
		return !(lock instanceof DummyLock);
	}
	
	/**
	 * Changes the database connection that that this {@code DatabaseAdapter} uses.
	 * 
	 * @param newConn	The new database connection that this {@code DatabaseAdapter}
	 * 					shall use.
	 * @throws IllegalStateException If this {@code DatabaseAdapter} is thread-safe.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	@Override
	public void setConnection(Connection newConn) throws SQLInPUTException {
		DatabaseInitializer initializer = new DatabaseInitializer(newConn);
		
		if (!(lock instanceof DummyLock))
			throw new IllegalStateException(
				"The 'setConnection' method shouldn't be " +
				"called on a thread-safe 'DatabaseAdapter'.");
		
		if (!initializer.databaseIsInitialized())
			initializer.initDatabase();
		
		exporter.setConnection(newConn);
		importer.setConnection(newConn);
		updater.setConnection(newConn);
		deleter.setConnection(newConn);
		
		super.setConnection(newConn);
	}
	
	/**
	 * Checks whether the given {@link Savepoint} is {@code null} and,
	 * if it isn't, undoes all changes made in the current transaction
	 * of the {@link Connection} after the savepoint was set.
	 * 
	 * @param sp	The savepoint that the current transaction of the
	 * 				{@code Connection} shall be rolled back to.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public void rollback(Savepoint sp) throws SQLInPUTException {
		lock.lock();
		
		try {
			rollback(conn, sp);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Checks whether the given {@link Boolean} is {@code null} and, if it isn't,
	 * sets the auto commit mode of the {@link Connection} in accordance with the
	 * {@code Boolean}.
	 * 
	 * @param autoCommit	{@code true} if the {@code Connection} shall be auto
	 * 						committing, {@code false} if it shall <i>not</i> be
	 * 						auto committing, or {@code null} if its auto commit 
	 * 						mode shall remain unchanged.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public void setAutoCommit(Boolean autoCommit) throws SQLInPUTException {
		lock.lock();
		
		try {
			setAutoCommit(conn, autoCommit);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Updates the specified InPUT XML document in the connected database.
	 * 
	 * @param doc	An InPUT XML document that has the same ID and type as
	 * 				the one that shall be updated and that contains the new
	 * 				content.
	 * @return	{@code true} if the document was successfully updated, or
	 * 			{@code false} if it doesn't have the same ID and type as
	 * 			any of the InPUT XML documents in the database.
	 * @throws InPUTException	If the document isn't an InPUT XML document,
	 * 							or if it's dependent on an document that
	 * 							cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public boolean update(Document doc) throws InPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return updater.update(doc);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Updates the specified {@link Exportable} in the connected database.
	 * 
	 * @param exportable	An {@code Exportable} that has the same ID and type as the
	 * 						one that shall be updated and that contains the new content.
	 * @return	{@code true} if the {@code Exportable} was successfully updated, or
	 * 			{@code false} if it doesn't have the same ID and type as any of the
	 * 			{@code Exportable}:s in the database.
	 * @throws InPUTException	If the {@code Exportable} isn't a design, design space,
	 * 							code-mapping object, experiment or {@link IInPUT}, or if
	 * 							it's dependent on code mappings that cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public boolean update(Exportable exportable) throws InPUTException, SQLInPUTException {
		lock.lock();
		
		try {
			return updater.update(exportable);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * A dummy {@link Lock} whose methods are no-ops.
	 */
	private static class DummyLock implements Lock {

		/**
		 * No-op
		 */
		public void lock() {}

		/**
		 * No-op
		 */
		public void lockInterruptibly() {}

		/**
		 * No-op
		 * 
		 * @return {@code false}
		 */
		public boolean tryLock() {
			return false;
		}

		/**
		 * No-op
		 * 
		 * @param time Isn't used.
		 * @param unit Isn't used.
		 * @return {@code false}
		 */
		public boolean tryLock(long time, TimeUnit unit) {
			return false;
		}

		/**
		 * No-op
		 */
		public void unlock() {}

		/**
		 * No-op
		 * 
		 * @return {@code null}
		 */
		public Condition newCondition() {
			return null;
		}
	}
}
