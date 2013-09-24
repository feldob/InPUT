package se.miun.itm.input.util.sql;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An uninstantiable class with a static immutable {@link Map} that maps the names
 * of all fields of the class {@link java.sql.Types} into their values. In addition,
 * the {@code Map} maps "XML" into {@code java.sql.Types.SQLXML} and "BYTEA" into
 * {@code java.sql.Types.BINARY}.
 * 
 * @ThreadSafe
 * @author Stefan Karlsson
 */
public abstract class Types {

	/**
	 * An immutable {@link Map} that maps the names of all fields of the class
	 * {@link java.sql.Types} into their values. In addition, the {@code Map}
	 * maps "XML" into {@code java.sql.Types.SQLXML} and "BYTEA" into
	 * {@code java.sql.Types.BINARY}.
	 */
	public static final Map<String, Integer> TYPES;
	
	static {
		Field[] fields = java.sql.Types.class.getFields();
		Map<String, Integer> types =
			new LinkedHashMap<String, Integer>(fields.length + 6);
		
		try {
			
			for (Field f : fields)
				types.put(f.getName(), f.getInt(null));
			
		} catch (IllegalAccessException e) {
			// This code should be unreachable.
			e.printStackTrace();
			System.exit(1);
		}

		types.put("XML", java.sql.Types.SQLXML);
		types.put("BYTEA", java.sql.Types.BINARY);
		
		TYPES = Collections.unmodifiableMap(types);
	}

	/**
	 * Throws an {@link UnsupportedOperationException}.
	 */
	private Types() {
		throw new UnsupportedOperationException("The class 'Types' is uninstantiable.");
	}
}
