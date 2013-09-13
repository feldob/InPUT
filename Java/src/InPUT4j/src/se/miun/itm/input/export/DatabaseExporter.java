package se.miun.itm.input.export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Types;
import java.util.Set;
import java.util.UUID;

import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.InPUTDocument;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.Framework;
import se.miun.itm.input.util.Q;
import se.miun.itm.input.util.sql.AbstractDatabaseConnector;
import se.miun.itm.input.util.sql.DatabaseAdapter;
import se.miun.itm.input.util.sql.DatabaseUpdater;

/**
 * Used for exporting {@link Document}:s and
 * {@link Exportable}:s to SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseExporter extends AbstractDatabaseConnector implements InPUTExporter<Boolean> {
	
	/**
	 * Used for inserting designs into the connected database.
	 */
	private PreparedStatement insertDesign;
	
	/**
	 * Used for inserting frameworks into the connected database.
	 */
	private PreparedStatement insertFramework;
	
	/**
	 * Used for inserting code mappings into the connected database.
	 */
	private PreparedStatement insertMappings;
	
	/**
	 * Used for inserting design spaces into the connected database.
	 */
	private PreparedStatement insertSpace;
	
	/**
	 * Constructs a {@code DatabaseExporter} that exports
	 * {@link Document}:s and {@link Exportable}:s to the
	 * designated database.
	 * 
	 * @param conn	A connection to the database that objects
	 * 				shall be exported to.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseExporter(Connection conn) throws SQLInPUTException {
		super(conn);
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
	public Boolean export(Document doc) throws InPUTException, SQLInPUTException {
		boolean exportComplete = false;
		Boolean oldAutoCommit = null;
		InPUTDocument inPUTDoc = new InPUTDocument(doc);
		Savepoint sp1 = null;
		SQLXML xml;
		String docID = inPUTDoc.getId();
		
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp1 = conn.setSavepoint();
			
			xml = conn.createSQLXML();
			xml.setString(inPUTDoc.toString());
			
			if (inPUTDoc.isDesign()) {
				InPUTDocument space = inPUTDoc.locateDesignSpace();

				if (space == null)
					throw new InPUTException(
						"Couldn't find the design space of the design.");
				else if (!export(space))
					new DatabaseUpdater(conn).update(space);

				insertDesign.setString(1, docID);
				insertDesign.setString(2, space.getId());
				insertDesign.setSQLXML(3, xml);
				insertDesign.executeUpdate();
			} else if (inPUTDoc.isDesignSpace()) {
				insertSpace.setString(1, docID);
				insertSpace.setSQLXML(2, xml);
				insertSpace.executeUpdate();
		
				if (inPUTDoc.hasStructuralParameters()) {
					Set<InPUTDocument> mappings = inPUTDoc.locateMappings();
					
					if (mappings.isEmpty()) 
						throw new InPUTException(
							"Couldn't find any code mappings of the design space.");
					
					for (InPUTDocument m : mappings) {
						Framework fw = Framework.frameworkOf(m);
						String version = m.getRootElement().
							getAttributeValue(Q.VERSION_ATTR);
						
						xml = conn.createSQLXML();
						xml.setString(m.toString());
					
						insertMappings.setString(1, m.getId());
						insertMappings.setString(2, version != null ?
							version : UUID.randomUUID().toString());
					
						if (fw == null)
							insertMappings.setNull(3, Types.VARCHAR);
						else {
							Savepoint sp2 = conn.setSavepoint();
							
							try {
								insertFramework.setString(1, fw.getId());
								insertFramework.executeUpdate();
							} catch (SQLException e) {
								if (e.getSQLState().equals(Q.SQL_STATE_UNIQUE_VIOLATION))
									DatabaseAdapter.rollback(conn, sp2);
								else
									throw new SQLInPUTException(
										DatabaseAdapter.concatSQLMessages(e), e);
							}
							
							insertMappings.setString(3, fw.getId());
						}
					
						insertMappings.setString(4, docID);
						insertMappings.setSQLXML(5, xml);
						insertMappings.executeUpdate();
					}
				}
			} else
				throw new InPUTException(
					"Export of '" + inPUTDoc.getInPUTType() +
					"' documents is unsupported by 'DatabaseExporter'.");
			
			if (oldAutoCommit)
				conn.commit();
			
			exportComplete = true;
		} catch (SQLException e) {
			
			if (e.getSQLState().equals(Q.SQL_STATE_UNIQUE_VIOLATION))
				return false;

			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			
			if (!exportComplete)
				DatabaseAdapter.rollback(conn, sp1);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
		}
		
		return true;
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
	public Boolean export(Exportable exportable) throws InPUTException, SQLInPUTException {
		boolean exportComplete = false;
		Boolean oldAutoCommit = null;
		Savepoint sp = null;
		
		if (exportable instanceof IDesignSpace)
			return export(exportable.export(new DocumentExporter()));
		
		try {
			oldAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			sp = conn.setSavepoint();
			
			if (exportable instanceof IDesign)
				exportComplete = exportDesign((IDesign) exportable);
			else if (exportable instanceof IExperiment)
				exportComplete = exportExperiment((IExperiment) exportable);
			else if (exportable instanceof IInPUT)
				exportComplete = exportInPUT((IInPUT) exportable);
			else
				throw new InPUTException(
					"Export of '" + exportable.getClass().getName() + 
					"' objects is unsupported by 'DatabaseExporter'.");

			if (oldAutoCommit && exportComplete)
				conn.commit();

		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {

			if (!exportComplete)
				DatabaseAdapter.rollback(conn, sp);
			
			DatabaseAdapter.setAutoCommit(conn, oldAutoCommit);
		}
		
		return exportComplete;
	}
	
	/**
	 * Returns export details about this {@code DatabaseExporter}.
	 */
	public String getInfo() {
		return	"Exports 'Document' and 'Exportable' objects to " +
				"a SQL database via the connection '" + conn + "'.";
	}
	
	/**
	 * Releases this {@code DatabaseExporter}'s database and JDBC resources.
	 * 
	 * @throws Throwable If a database error occurs.
	 */
	@Override
	protected void finalize() throws Throwable {
		DatabaseAdapter.close(insertDesign, insertSpace, insertMappings, insertFramework);
		super.finalize();
	}
	
	@Override
	protected void prepareStatements() throws SQLInPUTException {
		
		try {
			insertDesign = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".design " +
				"VALUES (?, ?, ?)");
			insertSpace = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".design_space " +
				"VALUES (?, ?)");
			insertMappings = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".mappings " +
				"VALUES (?, ?, 'Java', ?, ?, ?)");
			insertFramework = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".framework " +
				"VALUES (?)");
		} catch (SQLException e) {
			DatabaseAdapter.close(insertDesign, insertSpace, insertMappings);
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
	
	/**
	 * Exports the given design to the connected database.
	 * 
	 * @param design The design to be exported.
	 * @return	{@code true} if the design was successfully
	 * 			exported, or {@code false} if a design with
	 * 			the same ID is already in the database.
	 * @throws InPUTException	If a non-database export
	 * 							error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean exportDesign(IDesign design) throws InPUTException, SQLInPUTException {
		IDesignSpace space = design.getSpace();
		SQLXML xml;
		
		try {
			
			if (!export(space))
				new DatabaseAdapter(conn).update(space);
			
			xml = conn.createSQLXML();
			xml.setString(InPUTDocument.outputXMLString(
				design.export(new DocumentExporter())));

			insertDesign.setString(1, design.getId());
			insertDesign.setString(2, space.getId());
			insertDesign.setSQLXML(3, xml);
			insertDesign.executeUpdate();
		} catch (SQLException e) {
			
			if (e.getSQLState().equals(Q.SQL_STATE_UNIQUE_VIOLATION))
				return false;

			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
		
		return true;
	}

	/**
	 * Exports the given experiment to the connected database.
	 * 
	 * @param experiment The experiment to be exported.
	 * @return	{@code true} if the experiment was successfully
	 * 			exported, or {@code false} if an experiment with
	 * 			the same ID is already in the database.
	 * @throws InPUTException If a non-database export error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean exportExperiment(IExperiment experiment) throws InPUTException, SQLInPUTException {
		DatabaseUpdater updater = new DatabaseUpdater(conn);
		IDesign[] designs = new IDesign[] {
			experiment.getAlgorithmDesign(),
			experiment.getProblemFeatures(),
			experiment.getPreferences()
		};
		IInPUT inPUT = experiment.getInPUT();
		PreparedStatement	insertContent = null,
							insertExperiment = null,
							insertOutput = null;
		String experimentID = experiment.getId();
		
		try {
			insertExperiment = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".experiment " +
				"VALUES ('" + experimentID + "', ?, ?, ?, ?)");
				
			if (!export(inPUT))
				updater.update(inPUT);
				
			insertExperiment.setString(1, inPUT.getId());
			
			/* Exports the algorithm design, problem features and
			 * preferences of the experiment to the database. */
			for (int i = 0; i < designs.length; i++) {

				if (designs[i] == null)
					insertExperiment.setNull(i + 2, Types.VARCHAR);
				else {
					
					if (!export(designs[i]))
						updater.update(designs[i]);
					
					insertExperiment.setString(i + 2, designs[i].getId());
				}
			}

			insertExperiment.executeUpdate();

			insertContent = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".experiment_content " +
				"VALUES ('" + experimentID + "', ?, ?)");

			// Exports the contents of the experiment to the database.
			for (String contentName : experiment.getContentNames()) {
				insertContent.setString(1, contentName);
				insertContent.setBytes(2, experiment.getContentFor(contentName).toByteArray());
				insertContent.executeUpdate();
			}
			
			insertOutput = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".experiment_output " +
				"VALUES ('" + experimentID + "', ?)");

			// Exports the outputs of the experiment to the database.
			for (IDesign output : experiment.getOutput()) {
				
				if (!export(output))
					updater.update(output);

				insertOutput.setString(1, output.getId());
				insertOutput.executeUpdate();
			}
		} catch (SQLException e) {
			
			if (e.getSQLState().equals(Q.SQL_STATE_UNIQUE_VIOLATION))
				return false;

			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(insertExperiment, insertContent, insertOutput);
		}
		
		return true;
	}
	
	/**
	 * Exports the given {@link IInPUT} to the connected database.
	 * 
	 * @param inPUT The {@code IInPUT} to be exported.
	 * @return	{@code true} if the {@code IInPUT} was successfully exported,
	 * 			or {@code false} if an {@code IInPUT} with the same ID is
	 * 			already in the database.
	 * @throws InPUTException If a non-database export error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	private boolean exportInPUT(IInPUT inPUT) throws InPUTException, SQLInPUTException {
		DatabaseUpdater updater = new DatabaseUpdater(conn);
		IDesignSpace[] designSpaces = new IDesignSpace[] {
			inPUT.getAlgorithmDesignSpace(),
			inPUT.getPropertySpace(),
			inPUT.getProblemFeatureSpace(),
			inPUT.getOutputSpace()	
		};
		PreparedStatement insertInPUT = null;
		
		try {
			insertInPUT = conn.prepareStatement(
				"INSERT INTO " + Q.SQL_SCHEMA_NAME + ".input " +
				"VALUES ('" + inPUT.getId() + "', ?, ?, ?, ?)");
			
			// Exports the design spaces of the descriptor to the database.
			for (int i = 0; i < designSpaces.length; i++) {
					
				if (designSpaces[i] == null) 
					insertInPUT.setNull(i + 1, Types.VARCHAR);
				else {
						
					if (!export(designSpaces[i]))
						updater.update(designSpaces[i]);
						
					insertInPUT.setString(i + 1, designSpaces[i].getId());
				}
			}
			
			insertInPUT.executeUpdate();
		} catch (SQLException e) {
			
			if (e.getSQLState().equals(Q.SQL_STATE_UNIQUE_VIOLATION))
				return false;

			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(insertInPUT);
		}
		
		return true;
	}
}