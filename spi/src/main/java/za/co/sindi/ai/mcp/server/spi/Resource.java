package za.co.sindi.ai.mcp.server.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Buhake Sindi
 * @since 03 April 2025
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Resource {
	
	/**
	 * Human-readable name of the resource.
	 */
	String name() default "";
	
	/**
	 * Human-readable title of the resource.
	 * 
	 * <p />If not provided, the name should be used for display.
	 */
	String title() default "";
	
	/**
	 * A description of what this resource represents.<p />
     *
     * This can be used by clients to improve the LLM's understanding of available resources. It can be thought of like a "hint" to the model.
	 * @return
	 */
	String description() default "";
	
	/**
	 * The URI of this resource.
	 */
	String uri() default "";
	
	/**
	 * The MIME type of this resource, if known.
	 */
	String mimeType() default "";
	
	/**
     * Optional set of sized icons that the client can display in a user interface.<p />
     *
     * Clients that support rendering icons MUST support at least the following MIME types:
     * <pre>
     * - `image/png` - PNG images (safe, universal compatibility)
     * - `image/jpeg` (and `image/jpg`) - JPEG images (safe, universal compatibility)
     * </pre>
     *
     * Clients that support rendering icons SHOULD also support:
     * <pre>
     * - `image/svg+xml` - SVG images (scalable but requires security precautions)
     * - `image/webp` - WebP images (modern, efficient format)
     * </pre>
     */
	Icon[] icons() default {};
	
	/**
     * Optional additional annotations for the client.
     *
     */
	Annotations annotations() default @Annotations;
}
