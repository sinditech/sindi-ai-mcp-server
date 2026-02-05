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
public @interface ResourceTemplate {
	
	/**
	 * Human-readable name of the resource template.
	 */
	String name() default "";
	
	/**
	 * Human-readable title of the resource template.
	 * 
	 * <p />If not provided, the name should be used for display.
	 */
	String title() default "";
	
	/**
     * A description of what this template is for.
     *
     * <p />This can be used by clients to improve the LLM's understanding of available resources. It can be thought of like a "hint" to the model.
     */
	String description() default "";
	
	/**
	 *  A URI template (according to RFC 6570) that can be used to construct resource URIs.
	 */
	String uri() default "";
	
	/**
	 * The MIME type for all resources that match this template. This should only be included if all resources matching this template have the same type.
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
     * Optional additional for the client.
     *
     * Display name precedence order is: title, annotations.title, then name.
     */
	Annotations annotations() default @Annotations;
}
