package se.miun.itm.input.impOrt;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import se.miun.itm.input.Experiment;
import se.miun.itm.input.IExperiment;
import se.miun.itm.input.IInPUT;
import se.miun.itm.input.InPUT;
import se.miun.itm.input.InPUTConfig;
import se.miun.itm.input.aspects.Exportable;
import se.miun.itm.input.model.Document;
import se.miun.itm.input.model.IOInPUTException;
import se.miun.itm.input.model.InPUTDocument;
import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;
import se.miun.itm.input.model.design.DesignSpace;
import se.miun.itm.input.model.design.IDesign;
import se.miun.itm.input.model.design.IDesignSpace;
import se.miun.itm.input.model.mapping.Framework;
import se.miun.itm.input.model.mapping.Mappings;
import se.miun.itm.input.util.InputStreamWrapper;
import se.miun.itm.input.util.sql.AbstractDatabaseConnector;
import se.miun.itm.input.util.sql.DatabaseAdapter;
import se.miun.itm.input.util.xml.SAXUtil;

/**
 * Used for importing {@link Exportable}:s from SQL databases.
 * 
 * @NotThreadSafe
 * @author Stefan Karlsson
 */
public class DatabaseImporter<T extends Exportable>
	extends AbstractDatabaseConnector implements InPUTImporter<T> {
	
	/**
	 * The {@link Class} representing the type of {@link Exportable} 
	 * that the {@code impOrt()} method imports.
	 */
	private Class<T> type;
	
	/**
	 * Used for retrieving designs from databases.
	 */
	private PreparedStatement retrieveDesign;
	
	/**
	 * Used for retrieving non-frameworkless code mappings from databases.
	 */
	private PreparedStatement retrieveFrameworkMappings;
	
	/**
	 * Used for retrieving frameworkless code mappings from databases.
	 */
	private PreparedStatement retrieveMappings;
	
	/**
	 * Used for retrieving design spaces from databases.
	 */
	private PreparedStatement retrieveSpace;
	
	/**
	 * The ID of the {@link Exportable} that
	 * the {@code impOrt()} method imports.
	 */
	private String exportableID;
	
	/**
	 * The ID of the framework that the {@code impOrt()} method
	 * selects code mappings from, or {@code null} if the method
	 * selects code mappings that doesn't belong to any framework.
	 */
	private String frameworkID;
	
	/**
	 * Constructs a {@code DatabaseImporter} that imports
	 * {@link Exportable}:s from the designated database.
	 * 
	 * @param conn	A connection to the database that
	 * 				{@code Exportable}:s shall be imported
	 * 				from.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseImporter(Connection conn) throws SQLInPUTException {
		this(conn, null, null, null);
	}
	
	/**
	 * Constructs a {@code DatabaseImporter} that imports {@link Exportable}:s
	 * from the designated database and whose {@code impOrt()} method imports
	 * the specified {@code Exportable}.
	 * 
	 * @param conn	A connection to the database that {@code Exportable}:s
	 * 				shall be imported from.
	 * @param exportableID	The ID of the {@code Exportable} that the {@code impOrt()}
	 * 				method shall import.
	 * @param frameworkID	The ID of the framework that the {@code impOrt()} method
	 * 						shall select code mappings from, or {@code null} if the
	 * 						method shall select code mappings that doesn't belong to
	 * 						any framework.
	 * @param type	The {@link Class} representing the type of {@code Exportable}
	 * 				that the {@code impOrt()} method shall import.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public DatabaseImporter(Connection conn, String exportableID, String frameworkID, Class<T> type) throws SQLInPUTException {
		super(conn);
		this.exportableID = exportableID;
		this.frameworkID = frameworkID;
		this.type = type;
	}
	
	/**
	 * Creates a {@code DatabaseImporter} that imports {@link Exportable}:s
	 * from the designated database and whose {@code impOrt()} method imports
	 * the specified design.
	 * 
	 * @param conn	A connection to the database that {@code Exportable}:s
	 * 				shall be imported from.
	 * @param designID	The ID of the design that the {@code impOrt()} method
	 * 					shall import.
	 * @param frameworkID	The ID of the framework that the {@code impOrt()}
	 * 						method shall select code mappings from, or
	 * 						{@code null} if the method shall select code
	 * 						mappings that doesn't belong to any framework.
	 * @return The created {@code DatabaseImporter}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public static DatabaseImporter<IDesign> newDesignImporter(Connection conn, String designID, String frameworkID) throws SQLInPUTException {
		return new DatabaseImporter<IDesign>(conn, designID, frameworkID, IDesign.class);
	}
	
	/**
	 * Creates a {@code DatabaseImporter} that imports {@link Exportable}:s
	 * from the designated database and whose {@code impOrt()} method imports
	 * the specified design space.
	 * 
	 * @param conn	A connection to the database that {@code Exportable}:s
	 * 				shall be imported from.
	 * @param spaceID	The ID of the design space that the {@code impOrt()}
	 * 					method shall import.
	 * @param frameworkID	The ID of the framework that the {@code impOrt()}
	 * 						method shall select code mappings from, or
	 * 						{@code null} if the method shall select code
	 * 						mappings that doesn't belong to any framework.
	 * @return The created {@code DatabaseImporter}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public static DatabaseImporter<IDesignSpace> newDesignSpaceImporter(Connection conn, String spaceID, String frameworkID) throws SQLInPUTException {
		return new DatabaseImporter<IDesignSpace>(conn, spaceID, frameworkID, IDesignSpace.class);
	}
	
	/**
	 * Creates a {@code DatabaseImporter} that imports {@link Exportable}:s
	 * from the designated database and whose {@code impOrt()} method imports
	 * the specified experiment.
	 * 
	 * @param conn	A connection to the database that {@code Exportable}:s
	 * 				shall be imported from.
	 * @param experimentID	The ID of the experiment that the {@code impOrt()}
	 * 						method shall import.
	 * @param frameworkID	The ID of the framework that the {@code impOrt()}
	 * 						method shall select code mappings from, or
	 * 						{@code null} if the method shall select code
	 * 						mappings that doesn't belong to any framework.
	 * @return The created {@code DatabaseImporter}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public static DatabaseImporter<IExperiment> newExperimentImporter(Connection conn, String experimentID, String frameworkID) throws SQLInPUTException {
		return new DatabaseImporter<IExperiment>(conn, experimentID, frameworkID, IExperiment.class);
	}
	
	/**
	 * Creates a {@code DatabaseImporter} that imports {@link Exportable}:s
	 * from the designated database and whose {@code impOrt()} method imports
	 * the specified {@link IInPUT}.
	 * 
	 * @param conn	A connection to the database that {@code Exportable}:s
	 * 				shall be imported from.
	 * @param inPUTID	The ID of the {@code IInPUT} that the {@code impOrt()}
	 * 				method shall import.
	 * @param frameworkID	The ID of the framework that the {@code impOrt()}
	 * 						method shall select code mappings from, or
	 * 						{@code null} if the method shall select code
	 * 						mappings that doesn't belong to any framework.
	 * @return The created {@code DatabaseImporter}.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public static DatabaseImporter<IInPUT> newInPUTImporter(Connection conn, String inPUTID, String frameworkID) throws SQLInPUTException {
		return new DatabaseImporter<IInPUT>(conn, inPUTID, frameworkID, IInPUT.class);
	}
	
	/**
	 * Returns the ID of the {@link Exportable}
	 * that the {@code impOrt()} method imports.
	 */
	public String getExportableID() {
		return exportableID;
	}
	
	/**
	 * Returns the ID of the framework that the {@code impOrt()} method
	 * selects code mappings from, or {@code null} if the method selects
	 * code mappings that doesn't belong to any framework.
	 */
	public String getFrameworkID() {
		return frameworkID;
	}
	
	/**
	 * Returns import details about this {@code DatabaseImporter}.
	 */
	public String getInfo() {
		return	"Imports 'Exportable' objects from a SQL " +
				"database via the connection '" + conn + "'.";
	}
	
	/**
	 * Imports the {@link Exportable} that was specified when this
	 * {@code DatabaseImporter} was created.
	 * 
	 * @return	The specified {@code Exportable} if it's in the database,
	 * 			otherwise {@code null}. 
	 * @throws InPUTException	If the specified type isn't {@link IDesign},
	 * 							{@link IDesignSpace}, {@link IExperiment} or
	 * 							{@link IInPUT}, or if a non-IO non-database
	 * 							error occurs.
	 * @throws IOInPUTException If an I/O error occurs.
	 * @throws SQLInPUTException If a database error occurs.
	 */
	public T impOrt() throws InPUTException, IOInPUTException, SQLInPUTException {
		
		if (type == IDesign.class)
			return type.cast(importDesign(exportableID, frameworkID));
		else if (type == IDesignSpace.class)
			return type.cast(importDesignSpace(exportableID, frameworkID));
		else if (type == IExperiment.class)
			return type.cast(importExperiment(exportableID, frameworkID));
		else if (type == IInPUT.class)
			return type.cast(importInPUT(exportableID, frameworkID));
		else {
			throw new InPUTException(
				type == null ?
				"The constructor of the 'DatabaseImporter' " +
					"wasn't given any 'Class' object." :
				"Import of '" + type.getName() +
				"' objects is unsupported by 'DatabaseImporter'.");
		}
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
		IDesignSpace space;
		ResultSet rs = null;
		
		try {
			retrieveDesign.setString(1, designID);
			rs = retrieveDesign.executeQuery();

			if (!rs.next())
				return null;
			
			space = importDesignSpace(rs.getString("design_space"), frameworkID);
			
			return space == null ? null : space.impOrt(new InputStreamImporter(
					new BufferedInputStream(rs.getBinaryStream("content")),
					InPUTConfig.isValidationActive()));
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(rs);
		}
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
		InputStreamWrapper mappingStream, spaceStream; 
		ResultSet rs = null;
		
		try {
			retrieveSpace.setString(1, spaceID);
			rs = retrieveSpace.executeQuery();
			
			if (!rs.next())
				return null;

			spaceStream = new InputStreamWrapper(new BufferedInputStream(
				rs.getBinaryStream("content")));
			
			if (new InPUTDocument(SAXUtil.build(spaceStream.next(),
				InPUTConfig.isValidationActive())).hasStructuralParameters())
			{
				Document mappingDoc;
				PreparedStatement ps;
				
				if (frameworkID == null)
					ps = retrieveMappings;
				else {
					ps = retrieveFrameworkMappings;
					ps.setString(2, frameworkID);
				}

				ps.setString(1, spaceID);
				rs = ps.executeQuery();
				
				if (!rs.next())
					return null;
				
				mappingStream = new InputStreamWrapper(
					new BufferedInputStream(rs.getBinaryStream("content")));
				mappingDoc = SAXUtil.build(mappingStream.next(),
					InPUTConfig.isValidationActive());
				Mappings.initMapping(mappingDoc);
				
				if (frameworkID != null)
					synchronized (Framework.class) {
						Framework fw = Framework.getInstance(frameworkID);
					
						if (fw == null)
							fw = new Framework(frameworkID);
					
						fw.add(mappingDoc);
					}
				
				return new DesignSpace(spaceStream.next(),
					mappingStream.next());
			}
			
			return new DesignSpace(spaceStream.next());
		} catch (IOException e) {
			throw new IOInPUTException(e.getMessage(), e);
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(rs);
		}
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
		IInPUT inPUT;
		IExperiment experiment;
		ResultSet rs;
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment " +
				"WHERE id = '" + experimentID + "'");

			if (!rs.next())
				return null;
			
			inPUT = importInPUT(rs.getString("input"), frameworkID);
			
			if (inPUT == null)
				return null;
			
			experiment = new Experiment(experimentID, inPUT);
			
			/* Imports the algorithm design, problem features and
			 * preferences of the experiment from the database. */
			experiment.setAlgorithmDesign(importDesign(
				rs.getString("algorithm_design"), frameworkID));
			experiment.setProblemFeatures(importDesign(
				rs.getString("problem_features"), frameworkID));
			experiment.setPreferences(importDesign(
				rs.getString("preferences"), frameworkID));
			
			rs = stmt.executeQuery(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment_content " +
				"WHERE experiment = '" + experimentID + "'");
				
			// Imports the contents of the experiment from the database.
			while (rs.next())
				experiment.addContent(rs.getString("name"), InputStreamWrapper.init(
					new BufferedInputStream(rs.getBinaryStream("content"))));
			
			rs = stmt.executeQuery(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".experiment_output " +
				"WHERE experiment = '" + experimentID + "'");
			
			// Imports the outputs of the experiment from the database.
			while (rs.next()) {
				IDesign output = importDesign(rs.getString("output"), frameworkID);
				
				if (output != null)
					experiment.addOutput(output);
			}

		} catch (IOException e) {
			throw new IOInPUTException(e.getMessage(), e);
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
		
		return experiment;
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
		ResultSet rs;
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".input " +
				"WHERE id = '" + inPUTID + "'");
	
			return rs.next() ?
				new InPUT(
					inPUTID,
					importDesignSpace(rs.getString("algorithm_design_space"), frameworkID),
					importDesignSpace(rs.getString("property_space"), frameworkID),
					importDesignSpace(rs.getString("problem_feature_space"), frameworkID),
					importDesignSpace(rs.getString("output_space"), frameworkID)) :
				null;
		} catch (SQLException e) {
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		} finally {
			DatabaseAdapter.close(stmt);
		}
	}
	
	/**
	 * Changes the {@link Exportable} that is imported by the
	 * {@code impOrt()} method.
	 * 
	 * @param newId		The ID of the new {@code Exportable} that
	 * 					shall be imported by the {@code impOrt()}
	 * 					method.
	 */
	public void setExportableID(String newId) {
		exportableID = newId;
	}
	
	/**
	 * Changes the framework that the {@code impOrt()} method selects code
	 * mappings from.
	 * 
	 * @param newId		The ID of the framework that the {@code impOrt()} shall
	 * 					method select code mappings from, or {@code null} if the
	 * 					method shall select code mappings that doesn't belong to
	 * 					any framework.
	 */
	public void setFrameworkID(String newId) {
		frameworkID = newId;
	}
	
	/**
	 * Releases this {@code DatabaseExporter}'s database and JDBC resources.
	 * 
	 * @throws Throwable If a database error occurs.
	 */
	@Override
	protected void finalize() throws Throwable {
		DatabaseAdapter.close(
			retrieveDesign,
			retrieveSpace,
			retrieveMappings,
			retrieveFrameworkMappings);
		super.finalize();
	}
	
	@Override
	protected void prepareStatements() throws SQLInPUTException {
		
		try {
			retrieveDesign = conn.prepareStatement(
				"SELECT * FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".design " +
				"WHERE id = ?");
			retrieveSpace = conn.prepareStatement(
				"SELECT content FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".design_space " +
				"WHERE id = ?");
			retrieveMappings = conn.prepareStatement(
				"SELECT content FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".mappings " +
				"WHERE design_space = ? AND " +
					"framework IS NULL AND " +
					"programming_language = 'Java'");
			retrieveFrameworkMappings = conn.prepareStatement(
				"SELECT content FROM " + DatabaseAdapter.SQL_SCHEMA_NAME + ".mappings " +
				"WHERE design_space = ? AND " +
					"framework = ? AND " +
					"programming_language = 'Java'");
		} catch (SQLException e) {
			DatabaseAdapter.close(retrieveDesign, retrieveSpace, retrieveMappings);
			throw new SQLInPUTException(DatabaseAdapter.concatSQLMessages(e), e);
		}
	}
}
