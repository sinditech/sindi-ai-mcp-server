/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import za.co.sindi.ai.mcp.schema.Role;


/**
 * @author Buhake Sindi
 * @since 11 January 2026
 */
@Documented
@Retention(RUNTIME)
public @interface Annotations {
	/**
     * Describes who the intended audience of this object or data is.
     *
     * <p />It can include multiple entries to indicate content useful for multiple audiences (e.g., `["user", "assistant"]`).
     */
	Role[] audience() default {};

//	/**
//	 * Describes how important this data is for operating the server.
//	 *
//	 * <p />A value of 1 means "most important," and indicates that the data is
//	 * effectively required, while 0 means "least important," and indicates that
//	 * the data is entirely optional.
//	 *
//	 * @TJS-type number
//	 * @minimum 0
//	 * @maximum 1
//	 */
//	Double priority();
//
//	/**
//	 * The moment the resource was last modified, as an ISO 8601 formatted string.
//	 *
//	 * <p />Should be an ISO 8601 formatted string (e.g., "2025-01-12T15:00:58Z").
//	 *
//	 * <p />Examples: last activity timestamp in an open file, timestamp when the resource
//	 * was attached, etc.
//	 */
//	OffsetDateTime lastModified();
}
