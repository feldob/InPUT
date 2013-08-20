package se.miun.itm.input.util.sql;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import se.miun.itm.input.model.InPUTException;
import se.miun.itm.input.model.SQLInPUTException;

/**
 * Represents an immutable SQL table.
 * 
 * @ThreadSafe
 * @author Stefan Karlsson
 */
public class Table {

	/**
	 * An immutable list of metadata about the columns of this table.
	 */
	public final List<ColumnMetaDatum> COLUMN_META_DATA;
	
	/**
	 * An immutable list of the rows in this table.
	 */
	public final List<Row> ROWS;

	
	/**
	 * An immutable list of the indexes of this table.
	 */
	public final List<String> INDEXES;
	
	/**
	 * The definition of this table.
	 */
	public final String DEFINITION;
	
	/**
	 * The name of this table.
	 */
	public final String NAME;
	
	/**
	 * A {@link String} of identifier delimiters that is
	 * used by various SQL database management systems.
	 */
	private String identifierDelimiters = "\"'´`\\[\\]";
	
	/**
	 * A {@link Pattern} that matches identifier delimiters that
	 * is used by various SQL database management systems.
	 */
	private Pattern delimiterPattern = Pattern.compile(
		"[" + identifierDelimiters + "]");
	
	/**
	 * Constructs an empty {@code Table} with the given definition.
	 * 
	 * @param definition The definition of this table.
	 * @param rows The rows that shall be placed in this table.
	 * @throws InPUTException	If the definition cannot be parsed because of
	 * 							syntax errors.
	 */
	public Table(String definition) throws InPUTException {
		this(definition, null, new Row[0]);
	}
	
	/**
	 * Constructs an empty {@code Table} with the specified definition and indexes.
	 * 
	 * @param definition The definition of this table.
	 * @param indexes The definitions of the indexes of this table.
	 * @throws InPUTException	If:<br>
	 * 							<li>
	 * 								The table definition cannot be parsed because
	 * 								of syntax errors.
	 * 							</li>
	 * 							<li>
	 * 								Any of the index definitions cannot be parsed
	 * 								because of syntax errors.
	 * 							</li>
	 * 							<li>
	 * 								Any of the index definitions doesn't conform
	 * 								to the table definition.
	 * 							</li>
	 */
	public Table(String definition, String ... indexes) throws InPUTException {
		this(definition, indexes, new Row[0]);
	}
	
	/**
	 * Constructs a {@code Table} with the specified definition, indexes and rows.
	 * 
	 * @param definition The definition of this table.
	 * @param indexes	An array of the definitions of this table's indexes, or
	 * 					{@code null} if this table shall not have any indexes.
	 * @param rows The rows that shall be placed in this table.
	 * @throws InPUTException	If:<br>
	 * 							<li>
	 * 								The table definition cannot be parsed because
	 * 								of syntax errors.
	 * 							</li>
	 * 							<li>
	 * 								Any of the index definitions cannot be parsed
	 * 								because of syntax errors.
	 * 							</li>
	 * 							<li>
	 * 								Any of the index definitions doesn't conform
	 * 								to the table definition.
	 * 							</li>
	 * 							<li>
	 * 								Any of the rows doesn't conform to the
	 * 								table definition.
	 * 							</li>
	 * 							<li>
	 * 								The number of entries in any row isn't equal
	 * 								to the number of columns in this table.
	 * 							</li>
	 */
	public Table(String definition, String[] indexes, Row ... rows) throws InPUTException {
		String name;
		
		if (!definition.matches("(?i)\\s*CREATE\\s+.*TABLE.*\\(.+\\)"))
			throw new InPUTException(
				"The table definition couldn't be parsed because of syntax errors.");
		
		name = definition.substring(0, definition.indexOf('(')).trim();
		NAME = delimiterPattern.matcher(name.substring(Math.max(name.lastIndexOf(' '),
			name.lastIndexOf('.')) + 1)).replaceAll("");
		DEFINITION = definition;
		
		COLUMN_META_DATA = Collections.unmodifiableList(
			parseDefinition(definition));
		
		if (indexes == null)
			INDEXES = Collections.emptyList();
		else {
			
			if (!checkIndexes(indexes))
				throw new InPUTException(
					"All of the index definitions doesn't " +
					"conform to the table defintion.");
			
			INDEXES = Collections.unmodifiableList(
				Arrays.asList(indexes));
		}
		
		if (rows.length == 0)
			ROWS = Collections.emptyList();
		else {
			
			if (!checkRows(rows))
				throw new InPUTException(
					"All of the rows doesn't conform " +
					"to the table defintion.");
			
			ROWS = Collections.unmodifiableList(
				Arrays.asList(rows));
		}
	}
	
	/**
	 * Parses the given table definition.
	 * 
	 * @param definition The table definition that shall be parsed.
	 * @return Metadata about the columns of the defined table.
	 * @throws InPUTException	If the table definition cannot be
	 * 							parsed because of syntax errors.
	 */
	private List<ColumnMetaDatum> parseDefinition(String definition) throws InPUTException {
		ArrayList<ColumnMetaDatum> columnMetaData = new ArrayList<ColumnMetaDatum>();
		Pattern	charPattern = Pattern.compile(
					"(?i).+\\s+[^" + identifierDelimiters + "]*CHAR.*"),
				clobPattern = Pattern.compile(
					"(?i).+\\s+[^" + identifierDelimiters + "]*CLOB.*"),
				notNullPattern = Pattern.compile("(?i).+\\s+NOT\\s+NULL.*");
		Scanner sc = null;
		String token;
		
		try {
			/* Splits the table definition into an array of column
			 * definitions, and then loops through the array and
			 * parses the column definitions individually. */ 
			for (String columnDefinition : definition.substring(
				definition.indexOf('(') + 1, definition.lastIndexOf(')'))
				.split(","))
			{
				sc = new Scanner(columnDefinition);
				sc.useDelimiter("[\\s\\(\\)]+");
				token = sc.next().toUpperCase(Locale.ENGLISH);
				
				/* If a table constraint has been reached, all
				 * the column definitions have been parsed. */
				if (token.equals("CHECK") ||
					token.equals("CONSTRAINT") ||
					token.equals("FOREIGN") ||
					token.equals("PRIMARY") ||
					token.equals("UNIQUE"))
				{
					break;
				}
				
				// Parses the current column definition.
				while (true) {
					Integer type;
					String columnName;
					
					token = sc.next();
					type = Types.TYPES.get(token.toUpperCase(Locale.ENGLISH));

					if (type != null) {
						columnName = delimiterPattern.matcher(columnDefinition.
							substring(0, columnDefinition.indexOf(token))).
							replaceAll("").trim();
						
						columnMetaData.add(new ColumnMetaDatum(
							columnName,
							type,
							notNullPattern.matcher(columnDefinition).matches() ||
								definition.matches(
									"(?i).+,\\s*PRIMARY\\s+KEY\\s*" +
									"([^(]*" + columnName + "[^)]*).+") ?
								DatabaseMetaData.columnNoNulls :
								DatabaseMetaData.columnNullable,
							charPattern.matcher(columnDefinition).matches() ||
							clobPattern.matcher(columnDefinition).matches() ?
								sc.nextInt() : null));
						
						break;
					}
				}
			}
		} catch (Exception e) {
			columnMetaData.clear();
		} finally {
			sc.close();
		}
		
		if (columnMetaData.isEmpty())
			throw new InPUTException(
				"The table definition couldn't be parsed because of syntax errors.");
		
		columnMetaData.trimToSize();
		
		return columnMetaData;
	}
	
	/**
	 * Checks whether the given index definitions
	 * conforms to the definition of this table.
	 * 
	 * @param indexes	The index definitions that
	 * 					shall be verified.
	 * @return	{@code true} if the index definitions
	 * 			conforms to the definition of this
	 * 			table, otherwise {@code false}.
	 */
	private boolean checkIndexes(String ... indexes) {
		Pattern	indexPattern,
				whitespacePattern = Pattern.compile("\\s");
		Set<String> columnNames = new HashSet<String>(COLUMN_META_DATA.size());

		indexPattern = Pattern.compile(
			"(?i)\\s*CREATE\\s+.*INDEX\\s+.*" + NAME + ".*\\(.+\\)");
		
		for (ColumnMetaDatum metaDatum : COLUMN_META_DATA)
			columnNames.add(metaDatum.COLUMN_NAME.toUpperCase(Locale.ENGLISH));
		
		for (String def : indexes) {

			if (!indexPattern.matcher(def).matches())
				return false;

			for (String columnName : def.substring(def.indexOf('(') + 1,
				def.lastIndexOf(')')).split(","))
			{
				String[] tokens;
					
				columnName = columnName.trim();
				tokens = delimiterPattern.split(columnName);
					
				if (tokens.length > 1)
					columnName = tokens[1];
				else
					columnName = whitespacePattern.split(columnName)[0];
					
				if (!columnNames.contains(columnName.toUpperCase(Locale.ENGLISH)))
					return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks whether the given rows conforms to the
	 * definition of this table.
	 * 
	 * @param rows The rows that shall be verified.
	 * @return	{@code true} if the rows conforms to the
	 * 			definition of this table, otherwise
	 * 			{@code false}.
	 */
	private boolean checkRows(Row ... rows) {
		Map<String, ColumnMetaDatum> nameToDatum =
			new HashMap<String, ColumnMetaDatum>(COLUMN_META_DATA.size());

		for (ColumnMetaDatum metaDatum : COLUMN_META_DATA)
			nameToDatum.put(metaDatum.COLUMN_NAME.toUpperCase(Locale.ENGLISH), metaDatum);
		
		for (Row r : rows) {
			Set<String> columnNames =
				new HashSet<String>(COLUMN_META_DATA.size());
			
			for (Table.Entry<?> entry : r) {
				ColumnMetaDatum metaDatum = nameToDatum.get(
					entry.COLUMN_NAME.toUpperCase(Locale.ENGLISH));
				
				if (metaDatum == null ||
					metaDatum.DATA_TYPE != entry.DATA_TYPE ||
					metaDatum.NULLABLE == DatabaseMetaData.columnNoNulls &&
						entry.DATA == null)
				{
					return false;
				}
					
				columnNames.add(entry.COLUMN_NAME);
			}
			
			if (columnNames.size() != COLUMN_META_DATA.size())
				return false;
		}
		
		return true;
	}
	
	/**
	 * An immutable metadatum about a SQL table column.
	 * 
	 * @ThreadSafe
	 */
	public static class ColumnMetaDatum {

		/**
		 * The type of data that the column contains, which
		 * must be defined in the class {@link java.sql.Types}.
		 */
		public final int DATA_TYPE;
		
		/**
		 * Either {@code DatabaseMetaData.columnNullable} or
		 * {@code DatabaseMetaData.columnNoNulls}, depending
		 * on whether the column allows {@code NULL} values.
		 */
		public final int NULLABLE;
		
		/**
		 * If the type of data that the column contains is a character type,
		 * this is the maximum number of octets per datum in the column.
		 * Otherwise, it's {@code null}.
		 */
		public final Integer CHAR_OCTET_LENGTH;

		/**
		 * The name of the column.
		 */
		public final String COLUMN_NAME;
		
		/**
		 * Constructs a {@code ColumnMetaDatum} that describes a column with the
		 * specified characteristics.
		 * 
		 * @param columnName	The name of the column.
		 * @param dataType	The type of data that the columns contains, which
		 * 					must be defined in the class {@link java.sql.Types}.
		 * @param nullable	Either {@code DatabaseMetaData.columnNullable} or
		 * 					{@code DatabaseMetaData.columnNoNulls}, depending on
		 * 					whether the column allows {@code NULL} values.
		 * @param charOctetLength	If the type of data that the column contains
		 * 							is a character type, this is the maximum 
		 * 							number of octets per datum in the column. 
		 * 							Otherwise, it's ignored.
		 * @throws InPUTException	If the specified data type isn't one of the
		 * 							types defined in the class {@code java.sql.Types},
		 * 							or if the argument {@code nullable} isn't
		 * 							{@code DatabaseMetaData.columnNullable} or
		 * 							{@code DatabaseMetaData.columnNoNulls}.
		 */
		private ColumnMetaDatum(String columnName, int dataType, int nullable, Integer charOctetLength) throws InPUTException {

			if (!Types.TYPES.values().contains(dataType))
				throw new InPUTException(
					"The data type isn't be one of the types " +
					"defined in the class 'java.sql.Types'.");
			
			if (nullable != DatabaseMetaData.columnNullable &&
				nullable != DatabaseMetaData.columnNoNulls)
			{
				throw new InPUTException(
					"The argument 'nullable' must be 'DatabaseMetaData.columnNullable' " +
					"or 'DatabaseMetaData.columnNoNulls'.");
			}
				
			COLUMN_NAME = columnName;
			DATA_TYPE = dataType;
			NULLABLE = nullable;
			CHAR_OCTET_LENGTH = charOctetLength;
		}
	}
	
	/**
	 * Represents an immutable SQL table entry.
	 * 
	 * @ThreadSafe
	 */
	public static class Entry<T> {
	
		/**
		 * The type of the data of this entry, which must
		 * be defined in the class {@link java.sql.Types}.
		 */
		public final int DATA_TYPE;

		/**
		 * The data of this entry.
		 */
		public final T DATA;

		/**
		 * The name of the column in which this entry is placed.
		 */
		public final String COLUMN_NAME;

		/**
		 * A method that can be used to set parameters of
		 * {@link PreparedStatement}:s to the data of this entry.
		 */
		private final Method SETTER;
		
		/**
		 * Constructs an {@code Entry} with the given data of the specified type in
		 * the designated column. This entry uses the given method to set parameters
		 * of {@link PreparedStatement}:s to the data of this entry.
		 * 
		 * @param columnName The name of the column in which this entry is placed.
		 * @param dataType	The type of the data of this entry, which must be defined
		 *					in the class {@link java.sql.Types}.
		 * @param data The data of this entry.
		 * @param setter	A method that can be used to set parameters of
		 *					{@code PreparedStatement}:s to the data of this entry.
		 * @throws SQLInPUTException	If the specified data type isn't one of the
		 * 								types defined in the class {@code java.sql.Types},
		 * 								or if the given method isn't one of the
		 * 								{@code PreparedStatement} class' setter methods.
		 */
		public Entry (String columnName, int dataType, T data, Method setter) throws SQLInPUTException {

			if (!Types.TYPES.values().contains(dataType))
				throw new SQLInPUTException(
					"The data type isn't one of the types " +
					"defined in the class 'java.sql.Types'.");
			
			if (!setter.getName().startsWith("set") ||
				!Arrays.asList(PreparedStatement.class.getMethods()).contains(setter))
			{
				throw new SQLInPUTException(
					"The method isn't one of the 'PreparedStatement' " +
					"class' setter methods.");
			}
			
			COLUMN_NAME = columnName;
			DATA_TYPE = dataType;
			DATA = data;
			SETTER = setter;
		}
		
		/**
		 * Sets the designated parameter of the given
		 * {@link PreparedStatement} to the data of this entry.
		 * 
		 * @param ps	The {@code PreparedStatement} whose designated parameter
		 * 				shall be set to the data of this entry.
		 * @param index		The index of the parameter that shall be set to the
		 * 					data of this entry.
		 * @throws SQLInPUTException	If the parameter cannot be set to the
		 * 								data of this entry.
		 */
		public void setPreparedStatementParameter(PreparedStatement ps, int index) throws SQLInPUTException {
			try {
				SETTER.invoke(ps, index, DATA);
			} catch (Exception e) {
				throw new SQLInPUTException(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Represents an immutable SQL table row.
	 * 
	 * @ThreadSafe
	 */
	public static class Row extends AbstractList<Table.Entry<?>> {
		
		/**
		 * The entries in this row.
		 */
		private Entry<?>[] entries;

		/**
		 * Constructs an empty {@code Row}.
		 */
		public Row() {
			entries = new Entry[0];
		}
		
		/**
		 * Constructs a {@code Row} containing the same elements as the
		 * given {@link Collection}, in the order they are returned by the
		 * {@code Collection}'s iterator.
		 * 
		 * @param c		The {@code Collection} whose elements shall be
		 * 				placed in this row.
		 */
		public Row(Collection<? extends Row> c) {
			entries = c.toArray(new Entry[c.size()]);
		}
		
		/**
		 * Constructs a {@code Row} with the given entries.
		 * 
		 * @param entries The entries that this row shall contain.
		 */
		public Row (Entry<?> ... entries) {
			this.entries = entries;
		}
		
		@Override
		public Entry<?> get(int index) {
			return entries[index];
		}

		@Override
		public int size() {
			return entries.length;
		}
	}
}
