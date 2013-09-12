package se.miun.itm.input.util.sql;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import se.miun.itm.input.model.InPUTException;

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
	 * The definition of this table.
	 */
	public final String DEFINITION;
	
	/**
	 * The name of this table.
	 */
	public final String NAME;
	
	/**
	 * Constructs a {@code Table} with the specified definition.
	 * 
	 * @param definition The definition of this table.
	 * @throws InPUTException	If the table definition cannot be
	 * 							parsed because of syntax errors.
	 */
	public Table(String definition) throws InPUTException {
		ArrayList<ColumnMetaDatum> columnMetaData = new ArrayList<ColumnMetaDatum>();
		Pattern	charPattern = Pattern.compile("(?i)[^\0]+\\s+[^\"]*CHAR[^\0]*"),
				clobPattern = Pattern.compile("(?i)[^\0]+\\s+[^\"]*CLOB[^\0]*"),
				delimiterPattern = Pattern.compile("\""),
				notNullPattern = Pattern.compile("(?i)[^\0]+\\s+NOT\\s+NULL[^\0]*");
		Scanner sc = null;
		String commentlessDefinition, name, token;
		StringBuilder commentlessBuilder;
		
		commentlessDefinition = definition.replaceAll("--[^\n]*\n", "");
		commentlessBuilder = new StringBuilder(commentlessDefinition);
		
		while (true) {
			int i = commentlessBuilder.indexOf("/*");
			
			if (i >= 0)
				commentlessBuilder.delete(i, commentlessBuilder.indexOf("*/", i) + 2);
			else
				break;
		}
		
		commentlessDefinition = commentlessBuilder.toString();
		
		if (!commentlessDefinition.matches("(?i)\\s*CREATE\\s+[^\0]*TABLE[^\0]*\\([^\0]+\\)"))
			throw new InPUTException(
				"The table definition couldn't be parsed because of syntax errors.");
		
		name = commentlessDefinition.substring(0, commentlessDefinition.indexOf('(')).trim();
		name = delimiterPattern.matcher(name.substring(Math.max(name.lastIndexOf(' '),
			name.lastIndexOf('.')) + 1)).replaceAll("");
		
		try {
			/* Splits the table definition into an array of column
			 * definitions, and then loops through the array and
			 * parses the column definitions individually. */ 
			for (String columnDefinition : commentlessDefinition.substring(
				commentlessDefinition.indexOf('(') + 1, commentlessDefinition.lastIndexOf(')'))
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
								commentlessDefinition.matches(
									"(?i)[^\0]+,\\s*PRIMARY\\s+KEY\\s*" +
									"([^(]*" + columnName + "[^)]*)[^\0]+") ?
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
		
		NAME = name;
		DEFINITION = definition;
		COLUMN_META_DATA = Collections.unmodifiableList(columnMetaData);
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
}
