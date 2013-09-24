package se.miun.itm.input.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.export.DocumentExporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTDocument;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.Framework;
import se.miun.itm.input.model.mapping.IMappings;

/**
 * Used for deleting {@link Document}:s and {@link Exportable}:s from SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseDeleter extends AbstractDatabaseConnector {

	/**
	 * Used for deleting designs from databases.
	 */
	private PreparedStatement deleteDesign;
	
	/**
	 * Used for deleting non-frameworkless code mappings from databases.
	 */
	private PreparedStatement deleteFrameworkMappings;
	
	/**
	 * Used for deleting frameworkless code mappings from databases.
	 */
	private PreparedStatement deleteMappings;
	
	/**
	 * Used for deleting design spaces from databases.
	 */
	private PreparedStatement deleteSpace;
	
	/**
	 * Used for deleting the designs of specific
	 * design spaces from databases.
	 */
	private PreparedStatement deleteSpaceDesigns;
	
	/**
	 * Used for deleting the code mappings of
	 * specific design spaces from databases.
	 */
	private PreparedStatement deleteSpaceMappings;
	
	/**
	 * Constructs a {@code DatabaseDeleter} that deletes {@link Document}:s 
	 * and {@link Exportable}:s from the designated database.
	 * 
	 * @param conn	A connection to the database from which objects shall
	 * 				be deleted.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseDeleter(Connection conn) throws SQLInPUTException {
		super(conn);
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
		boolean deletionComplete = false;
		Boolean oldAutoCommit = null;
		InPUTDocument inPUTDoc = new InPUTDocument(doc);
		Savepoint sp = null;
		String	id = inPUTDoc.getId();
		
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();
			
			if (inPUTDoc.isDesign())
				deletionComplete = deleteDesign(id);
			else if (inPUTDoc.isDesignSpace())
				deletionComplete = deleteDesignSpace(id);
			else if (inPUTDoc.isMapping()) {
				Framework fw = Framework.frameworkOf(doc);
				deletionComplete = deleteMappings(id, fw == null ? null : fw.getId());
			} else
				throw new InPUTException(
					"Deletion of '" + inPUTDoc.getInPUTType() +
					"' documents is unsupported by 'DatabaseDeleter'.");
	
			if (oldAutoCommit && deletionComplete)
				conn.commit();
			
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!deletionComplete)
				DatabaseAdapter.rollback(conn, sp);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
		}
		
		return deletionComplete;
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
		boolean deletionComplete = false;
		Boolean oldAutoCommit = null;
		Savepoint sp = null;
		
		if (exportable instanceof IDesign ||
			exportable instanceof IDesignSpace ||
			exportable instanceof IMappings)
		{
			return delete(exportable.export(new DocumentExporter()));
		}
		
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();
			
			if (exportable instanceof IExperiment)
				deletionComplete = deleteExperiment(((IExperiment) exportable).getId());
			else if (exportable instanceof IInPUT)
				deletionComplete = deleteInPUT(((IInPUT) exportable).getId());
			else if (exportable instanceof Framework)
				deletionComplete = deleteFramework(((Framework) exportable).getId());
			else
				throw new InPUTException(
					"Deletion of '" + exportable.getClass().getName() +
					"' objects is unsupported by 'DatabaseDeleter'.");
	
			if (oldAutoCommit && deletionComplete)
				conn.commit();
			
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!deletionComplete)
				DatabaseAdapter.rollback(conn, sp);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
		}
		
		return deletionComplete;
	}
	
	/**
	 * Releases this {@code DatabaseDeleter}'s database and JDBC resources.
	 * 
	 * @throws Throwable If a database error occurs.
	 */
	@Override
	protected void finalize() throws Throwable {
		DatabaseAdapter.close(
			deleteDesign,
			deleteSpace,
			deleteSpaceDesigns,
			deleteSpaceMappings,
			deleteMappings,
			deleteFrameworkMappings);
		super.finalize();
	}
	
	@Override
	protected void prepareStatements() throws SQLInPUTException {
		
		try {
			deleteDesign = conn.prepareStatement(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".design " +
				"WHERE id = ?");
			deleteSpace = conn.prepareStatement(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".design_space " +
				"WHERE id = ?");
			deleteSpaceDesigns = conn.prepareStatement(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".design " +
				"WHERE design_space = ?");
			deleteSpaceMappings = conn.prepareStatement(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".mappings " +
				"WHERE design_space = ?");
			deleteMappings = conn.prepareStatement(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".mappings " +
				"WHERE id = ? AND " +
					"programming_language = 'Java' AND " +
					"framework IS NULL");
			deleteFrameworkMappings = conn.prepareStatement(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".mappings " +
				"WHERE id = ? AND " +
					"programming_language = 'Java' AND " +
					"framework = ?");
		} catch (SQLException e) {
			DatabaseAdapter.close(
				deleteDesign,
				deleteSpace,
				deleteSpaceDesigns,
				deleteSpaceMappings,
				deleteMappings);
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
	
	/**
	 * Deletes the specified framework from the connected database.
	 * 
	 * @param id The ID of the framework that shall be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the specified framework was not in the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean deleteFramework(String id) throws SQLInPUTException {
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".mappings " +
				"WHERE framework = '" + id + "'");

			return stmt.executeUpdate(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".framework " +
				"WHERE id = '" + id + "'") > 0;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
	}
	
	/**
	 * Deletes the specified design from the connected database.
	 * 
	 * @param id The ID of the design that shall be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the specified design was not in the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean deleteDesign(String id) throws SQLInPUTException {
		
		try {
			deleteDesign.setString(1, id);
			return deleteDesign.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
	
	/**
	 * Deletes the specified design space from the connected database.
	 * 
	 * @param id The ID of the design space that shall be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the specified design space was not in the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean deleteDesignSpace(String id) throws SQLInPUTException {

		try {
			deleteSpaceDesigns.setString(1, id);
			deleteSpaceDesigns.executeUpdate();
		
			deleteSpaceMappings.setString(1, id);
			deleteSpaceMappings.executeUpdate();
		
			deleteSpace.setString(1, id);
			return deleteSpace.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
	
	/**
	 * Deletes the specified experiment from the connected database.
	 * 
	 * @param id The ID of the experiment that shall be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the specified experiment was not in the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean deleteExperiment(String id) throws SQLInPUTException {
		List<String>	designIDs = new ArrayList<String>(3),
						outputIDs = new ArrayList<String>();
		ResultSet rs;
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
				
			rs = stmt.executeQuery(
				"SELECT output FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment_output " +
				"WHERE experiment = '" + id + "'");
				
			while (rs.next())
				outputIDs.add(rs.getString("output"));
				
			stmt.executeUpdate(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment_output " +
				"WHERE experiment = '" + id + "'");
			
			// Deletes the outputs of the experiment from the database.
			for (String oid : outputIDs)
				deleteDesign(oid);
				
			// Deletes the contents of the experiment from the database.
			stmt.executeUpdate(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment_content " +
				"WHERE experiment = '" + id + "'");
			
			rs = stmt.executeQuery(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment " +
				"WHERE id = '" + id + "'");
			
			if (!rs.next())
				return false;
				
			for (int i = 3; i < 6; i++) {
				String did = rs.getString(i);
					
				if (did != null)
					designIDs.add(did);
			}
				
			stmt.executeUpdate(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment " +
				"WHERE id = '" + id + "'");
				
			/* Deletes the algorithm design, problem features and
			 * preferences of the experiment from the database. */
			for (String did : designIDs)
				deleteDesign(did);
		
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
		
		return true;
	}
	
	/**
	 * Deletes the specified {@link IInPUT} from the connected database.
	 * 
	 * @param id The ID of the {@link IInPUT} that shall be deleted.
	 * @return	{@code true} if the deletion was successful, or {@code false}
	 * 			if the specified {@code IInPUT} was not in the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean deleteInPUT(String id) throws SQLInPUTException {
		List<String> spaceIDs = new ArrayList<String>(4);
		ResultSet rs;
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();

			rs = stmt.executeQuery(
				"SELECT id FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment " +
				"WHERE input = '" + id + "'");
			
			// Deletes the experiments of the 'IInPUT' from the database.
			while (rs.next())
				deleteExperiment(rs.getString("id"));
			
			rs = stmt.executeQuery(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".input " +
				"WHERE id = '" + id + "'");
			
			if (!rs.next())
				return false;
			
			for (int i = 2; i < 6; i++) {
				String dsid = rs.getString(i);
						
				if (dsid != null)
					spaceIDs.add(dsid);
			}
					
			stmt.executeUpdate(
				"DELETE FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".input " +
				"WHERE id = '" + id + "'");
					
			// Deletes the design spaces of the 'IInPUT' from the database.
			for (String dsid : spaceIDs)
				deleteDesignSpace(dsid);
				
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
		
		return true;
	}
	
	/**
	 * Deletes the specified code-mapping object from the connected database.
	 * 
	 * @param mappingID The ID of the code-mapping object that shall be deleted.
	 * @param frameworkID	The ID of the framework that the code-mapping object
	 * 						belongs to, or {@code null} if it doesn't belong to
	 * 						any framework.
	 * @return	{@code true} if the deletion was successful, or {@code false} if
	 * 			the specified code-mapping object was not in the database.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean deleteMappings(String mappingID, String frameworkID) throws SQLInPUTException {
		PreparedStatement ps;
		
		try {
			
			if (frameworkID == null)
				ps = deleteMappings;
			else {
				ps = deleteFrameworkMappings;
				ps.setString(2, frameworkID);
			}
			
			ps.setString(1, mappingID);
			
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
}
