/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import za.co.sindi.commons.utils.Primitives;

/**
 * @author Buhake Sindi
 * @since 09 May 2025
 */
public class JSONDataTypes {
	
	private static final Map<Class<?>, String> JAVA_DATA_TYPES;
	
	static {
		Map<Class<?>, String> types = new LinkedHashMap<>();
		types.put(Number.class, "number");
		types.put(Boolean.class, "boolean");
		types.put(Character.class, "string");
		types.put(CharSequence.class, "string");
		types.put(Collection.class, "array");
		types.put(Map.class, "object");
		types.put(Date.class, "string");
		types.put(Temporal.class, "string");
		types.put(UUID.class, "string");
		types.put(Object.class, "object");
		JAVA_DATA_TYPES = Collections.unmodifiableMap(types);
	}

	private JSONDataTypes() {
		throw new AssertionError("Private constructor.");
	}
	
	public static String deterimineJsonType(final Class<?> clazz) {
		if (clazz == null) return "null";
		
		final Class<?> type = clazz.isArray() ? Collection.class : clazz.isPrimitive() ? Primitives.wrap(clazz) : clazz ;
		Optional<String> jsonType = JAVA_DATA_TYPES.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(type)).map(Map.Entry::getValue).findFirst();
		return jsonType.orElseThrow(() -> new IllegalArgumentException("Could not map type '" + clazz + "' to a respective JSON type."));
	}
}
