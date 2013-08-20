package se.miun.itm.input.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.aspects.Identifiable;
import se.miun.itm.input.export.DatabaseExporter;
import se.miun.itm.input.export.DocumentExporter;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTDocument;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.Framework;
import se.miun.itm.input.model.mapping.IMappings;
import se.miun.itm.input.util.Q;

/**
 * Used for updating {@link Document}:s and {@link Exportable}:s in SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseUpdater extends AbstractDatabaseConnector {

	/**
	 * Used for updating designs in databases.
	 */
	private PreparedStatement updateDesign;
	
	/**
	 * Used for updating non-frameworkless code mappings in databases.
	 */
	private PreparedStatement updateFrameworkMappings;
	
	/**
	 * Used for updating frameworkless code mappings in databases.
	 */
	private PreparedStatement updateMappings;
	
	/**
	 * Used for updating design spaces in databases.
	 */
	private PreparedStatement updateSpace;
	
	/**
	 * Constructs a {@code DatabaseUpdater} that updates {@link Document}:s and
	 * {@link Exportable}:s in the designated database.
	 * 
	 * @param conn	A connection to the database in which objects shall be updated.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseUpdater(Connection conn) throws SQLInPUTException {
		super(conn);
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
		boolean updateComplete = false;
		Boolean oldAutoCommit = false;
		InPUTDocument inPUTDoc = new InPUTDocument(doc);
		Savepoint sp = null;
		SQLXML xml;
		String docID = inPUTDoc.getId();
		
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();
			
			xml = conn.createSQLXML();
			xml.setString(inPUTDoc.toString());
			
			if (inPUTDoc.isDesign()) {
				InPUTDocument spaceDoc = inPUTDoc.locateDesignSpace(conn);
				
				if (spaceDoc == null)
					throw new InPUTException(
						"Couldn't find the design space of the design.");
				
				if (!update(spaceDoc))
					new DatabaseExporter(conn).export(spaceDoc);
				
				updateDesign.setString(1, spaceDoc.getId());
				updateDesign.setSQLXML(2, xml);
				updateDesign.setString(3, docID);
				
				if (updateDesign.executeUpdate() == 0)
					return false;

			} else if (inPUTDoc.isDesignSpace()) {
				updateSpace.setSQLXML(1, xml);
				updateSpace.setString(2, docID);
				
				if (updateSpace.executeUpdate() == 0)
					return false;

				if (inPUTDoc.hasStructuralParameters()) {
					PreparedStatement insertMappings;
					Set<InPUTDocument> mappings = inPUTDoc.locateMappings();
					
					if (mappings.isEmpty())
						throw new InPUTException(
							"Couldn't find any code mappings of the design space.");
					
					insertMappings = conn.prepareStatement(
						"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".mappings " +
						"(id, programming_language, framework, design_space, content)" +
						"VALUES (?, 'Java', ?, " + docID + ", ?)");
					
					for (InPUTDocument m : mappings)
						if (!update(m)) {
							Framework fw = Framework.frameworkOf(m);
							
							xml = conn.createSQLXML();
							xml.setString(m.toString());

							insertMappings.setString(1, m.getId());
							
							if (fw == null)
								insertMappings.setNull(2, java.sql.Types.VARCHAR);
							else
								insertMappings.setString(2, fw.getId());
							
							insertMappings.setSQLXML(3, xml);
							insertMappings.executeUpdate();
						}
				}
			} else if (inPUTDoc.isMapping()) {
				Framework fw = Framework.frameworkOf(doc);
				PreparedStatement ps;
				
				if (fw == null)
					ps = updateMappings;
				else {
					ps = updateFrameworkMappings;
					ps.setString(3, fw.getId());
				}
				
				
				ps.setSQLXML(1, xml);
				ps.setString(2, docID);
				
				if (ps.executeUpdate() == 0)
					return false;
				
			} else
				throw new InPUTException(
					"Update of '" + inPUTDoc.getInPUTType() +
					"' documents is unsupported by 'DatabaseUpdater'.");
			
			if (oldAutoCommit)
				conn.commit();
			
			updateComplete = true;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!updateComplete)
				DatabaseAdapter.rollback(conn, sp);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
		}
		
		return true;
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
		boolean updateComplete = false;
		Boolean oldAutoCommit = null;
		Savepoint sp = null;
		
		if (exportable instanceof IDesignSpace || exportable instanceof IMappings)
			return update(exportable.export(new DocumentExporter()));
		
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();
			
			if (exportable instanceof IDesign)
				updateComplete = updateDesign((IDesign) exportable);
			else if (exportable instanceof IExperiment)
				updateComplete = updateExperiment((IExperiment) exportable);
			else if (exportable instanceof IInPUT)
				updateComplete = updateInPUT((IInPUT) exportable);
			else if (exportable instanceof Framework) {
				updateComplete = updateFramework((Framework) exportable);
			} else
				throw new InPUTException(
					"Updating of '" + exportable.getClass().getName() +
					"' objects is unsupported by 'DatabaseUpdater'.");
			
			if (oldAutoCommit && updateComplete)
				conn.commit();
			
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!updateComplete)
				DatabaseAdapter.rollback(conn, sp);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
		}
		
		return updateComplete;
	}
	
	/**
	 * Releases this {@code DatabaseUpdater}'s database and JDBC resources.
	 * 
	 * @throws Throwable If a database error occurs.
	 */
	@Override
	protected void finalize() throws Throwable {
		DatabaseAdapter.close(
			updateDesign,
			updateSpace,
			updateMappings,
			updateFrameworkMappings);
		super.finalize();
	}
	
	@Override
	protected void prepareStatements() throws SQLInPUTException {
		
		try {
			updateDesign = conn.prepareStatement(
				"UPDATE " + Q.SQL_SCHEMA_NAME + ".design " +
				"SET design_space = ?, " +
					"content = ? " +
				"WHERE id = ?");
			updateSpace = conn.prepareStatement(
				"UPDATE " + Q.SQL_SCHEMA_NAME + ".design_space " +
				"SET content = ? " +
				"WHERE id = ?");
			updateMappings = conn.prepareStatement(
				"UPDATE " + Q.SQL_SCHEMA_NAME + ".mappings " +
				"SET content = ? " +
				"WHERE id = ? AND " +
					"programming_language = 'Java' AND " +
					"framework IS NULL");
			updateFrameworkMappings = conn.prepareStatement(
				"UPDATE " + Q.SQL_SCHEMA_NAME + ".mappings " +
				"SET content = ? " +
				"WHERE id = ? AND " +
					"programming_language = 'Java' AND " +
					"framework = ?");
		} catch (SQLException e) {
			DatabaseAdapter.close(updateDesign, updateSpace, updateMappings);
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
	
	/**
	 * Updates the specified design in the connected database.
	 * 
	 * @param design	A design that has the same ID as the one that shall
	 * 					be updated and that contains the new content.
	 * @return	{@code true} if the design was successfully updated, or
	 * 			{@code false} if it doesn't have the same ID as any of the
	 * 			designs in the database.
	 * @throws InPUTException	If the design is dependent on code mappings
	 * 							that cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean updateDesign(IDesign design) throws InPUTException, SQLInPUTException {
		IDesignSpace space = design.getSpace();
		ResultSet rs = null;
		SQLXML xml;
		
		try {

			if (!update(space))
				new DatabaseExporter(conn).export(space);

			xml = conn.createSQLXML();
			xml.setString(InPUTDocument.outputXMLString(
				design.export(new DocumentExporter())));

			updateDesign.setString(1, space.getId());
			updateDesign.setSQLXML(2, xml);
			updateDesign.setString(3, design.getId());
			
			if (updateDesign.executeUpdate() == 0)
				return false;

		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(rs);
		}
		
		return true;
	}
	
	/**
	 * Updates the specified experiment in the connected database.
	 * 
	 * @param experiment	An experiment that has the same ID as
	 * 						the one that shall be updated and that
	 * 						contains the new content.
	 * @return	{@code true} if the experiment was successfully
	 * 			updated, or {@code false} if it doesn't have the
	 * 			same ID as any of the experiments in the database.
	 * @throws InPUTException	If the experiment is dependent on
	 * 							code mappings that cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean updateExperiment(IExperiment experiment) throws InPUTException, SQLInPUTException {
		DatabaseExporter exporter = new DatabaseExporter(conn);
		Map<String, Exportable> currentComponents =
			new LinkedHashMap<String, Exportable>(4);
		PreparedStatement	deleteOutput = null,
							insertContent = null,
							insertOutput = null;
		ResultSet rs;
		Set<String> oldOutputIDs = new HashSet<String>();
		Statement stmt = null;
		String experimentID = experiment.getId();
		StringBuilder updateBuilder = new StringBuilder(
			"UPDATE " + Q.SQL_SCHEMA_NAME + ".experiment " +
			"SET ");
		
		currentComponents.put("input", experiment.getInPUT());
		currentComponents.put("algorithm_design", experiment.getAlgorithmDesign());
		currentComponents.put("problem_features", experiment.getProblemFeatures());
		currentComponents.put("preferences", experiment.getPreferences());
		
		try {
			
			for (Map.Entry<String, Exportable> entry : currentComponents.entrySet()) {
				Exportable comp = entry.getValue();
				String compID;
				
				if (comp == null)
					compID = "NULL";
				else {
					compID = "'" + ((Identifiable) comp).getId() + "'";
					
					if (!update(comp))
						exporter.export(comp);
				}
				
				updateBuilder.append(entry.getKey() + " = " + compID + ", ");
			}
			
			updateBuilder.replace(
				updateBuilder.length() - 2,
				Integer.MAX_VALUE,
				" WHERE id = '" + experimentID + "'");
			
			stmt = conn.createStatement();
		
			if (stmt.executeUpdate(updateBuilder.toString()) == 0)
				return false;
			
			stmt.executeUpdate(
				"DELETE FROM " + Q.SQL_SCHEMA_NAME + ".experiment_content " +
				"WHERE experiment = '" + experimentID + "'");
		
			insertContent = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".experiment_content " +
				"VALUES ('" + experimentID + "', ?, ?)");

			// Exports the current contents of the experiment to the database.
			for (String contentName : experiment.getContentNames()) {
				insertContent.setString(1, contentName);
				insertContent.setBytes(2,
					experiment.getContentFor(contentName).toByteArray());
				insertContent.executeUpdate();
			}
			
			rs = stmt.executeQuery(
				"SELECT output FROM " + Q.SQL_SCHEMA_NAME + ".experiment_output " +
				"WHERE experiment = '" + experimentID + "'");
			
			while (rs.next())
				oldOutputIDs.add(rs.getString("output"));
			
			insertOutput = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".experiment_output " +
				"VALUES ('" + experimentID + "', ?)");
			
			// Exports the current outputs of the experiment to the database.
			for (IDesign output : experiment.getOutput()) {

				if (oldOutputIDs.remove(output.getId()))
					updateDesign(output);
				else {
					
					if (!exporter.export(output))
						updateDesign(output);

					insertOutput.setString(1, output.getId());
					insertOutput.executeUpdate();
				}
			}
			
			if (!oldOutputIDs.isEmpty()) {
				deleteOutput = conn.prepareStatement(
					"DELETE FROM " + Q.SQL_SCHEMA_NAME + ".experiment_output " +
					"WHERE experiment = '" + experimentID + "' " +
						" AND output = ?");
			
				for (String id : oldOutputIDs) {
					deleteOutput.setString(1, id);
					deleteOutput.executeUpdate();
				}
			}
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt, insertContent, insertOutput, deleteOutput);
		}
		
		return true;
	}
	
	/**
	 * Updates the specified framework in the connected database.
	 * 
	 * @param fw	A framework that has the same ID as the one
	 * 				that shall be updated and that contains the
	 * 				new/updated elements.
	 * @return	{@code true} if the framework was successfully
	 * 			updated, or {@code false} if it doesn't have the
	 * 			same ID as any of the frameworks in the database.
	 * @throws InPUTException	If any of the code-mapped design
	 * 							spaces cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean updateFramework(Framework fw) throws InPUTException, SQLInPUTException {
		DatabaseExporter exporter;
		ResultSet rs;
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
				"SELECT * FROM " + Q.SQL_SCHEMA_NAME + ".framework " +
				"WHERE id = '" + fw.getId() + "'");
			
			if (!rs.next())
				return false;
			
			exporter = new DatabaseExporter(conn);
			
			for (Document doc : fw)
				if (!update(doc))
					exporter.export(doc);
			
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
		
		return true;
	}
	
	/**
	 * Updates the specified {@link IInPUT} in the connected database.
	 * 
	 * @param inPUT		An {@code IInPUT} that has the same ID as the one that
	 * 					shall be updated and that contains the new content.
	 * @return	{@code true} if the {@code IInPUT} was successfully updated, or 
	 * 			{@code false} if it doesn't have the same ID as any of the
	 * 			{@code IInPUT}:s in the database.
	 * @throws InPUTException	If the {@code IInPUT} is dependent on code
	 * 							mappings that cannot be found.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean updateInPUT(IInPUT inPUT) throws InPUTException, SQLInPUTException {
		DatabaseExporter exporter = new DatabaseExporter(conn);
		Map<String, IDesignSpace> currentSpaces =
			new LinkedHashMap<String, IDesignSpace>(4);
		Statement stmt = null;
		String inPUTID = inPUT.getId();
		StringBuilder updateBuilder = new StringBuilder(
			"UPDATE " + Q.SQL_SCHEMA_NAME + ".input " +
			"SET ");
		
		currentSpaces.put("algorithm_design_space", inPUT.getAlgorithmDesignSpace());
		currentSpaces.put("property_space", inPUT.getPropertySpace());
		currentSpaces.put("problem_feature_space", inPUT.getProblemFeatureSpace());
		currentSpaces.put("output_space", inPUT.getOutputSpace());
		
		try {
			
			for (Map.Entry<String, IDesignSpace> entry : currentSpaces.entrySet()) {
				IDesignSpace space = entry.getValue();
				String spaceID;
				
				if (space == null)
					spaceID = "NULL";
				else {
					spaceID = "'" + space.getId() + "'";
					
					if (!update(space))
						exporter.export(space);
				}
				
				updateBuilder.append(entry.getKey() + " = " + spaceID + ", ");
			}
			
			updateBuilder.replace(
				updateBuilder.length() - 2,
				Integer.MAX_VALUE,
				" WHERE id = '" + inPUTID + "'");
			
			stmt = conn.createStatement();
		
			if (stmt.executeUpdate(updateBuilder.toString()) == 0)
				return false;

		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
		
		return true;
	}
}
