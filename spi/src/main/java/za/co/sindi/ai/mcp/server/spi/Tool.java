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
public @interface Tool {
	
	/**
     * Name of the tool. If not provided, method name will be used.
     *
     */
	String name() default "";
	
	/**
     * Title of the tool.
     *
     */
	String title() default "";
	
	/**
     * Description of the tool.
     * <br />It should be clear and descriptive to allow language model to understand the tool's purpose and its intended use.
     *
     * @return description of the tool.
     */
	String description() default "";
	
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
     * Optional additional tool information.
     *
     * Display name precedence order is: title, annotations.title, then name.
     */
	ToolAnnotations annotations() default @ToolAnnotations;
	
	@Documented
	@Retention(RUNTIME)
	public @interface ToolAnnotations {
		
		/**
	     * A human-readable title for the tool.
	     *
	     */
		String title() default "";
		
		/**
		 * If true, the tool does not modify its environment.
		 *
		 * Default: false
		 */
		boolean readOnlyHint() default false;
		
		/**
	     * If true, the tool may perform destructive updates to its environment.
		 * If false, the tool performs only additive updates.
		 *
		 * (This property is meaningful only when `readOnlyHint == false`)
		 *
		 * Default: true
		 */
		boolean destructiveHint() default true;

		/**
		 * If true, calling the tool repeatedly with the same arguments
		 * will have no additional effect on its environment.
		 *
		 * (This property is meaningful only when `readOnlyHint == false`)
		 *
		 * Default: false
		 */
		boolean idempotentHint() default false;
		
		/**
		 * If true, this tool may interact with an "open world" of external
		 * entities. If false, the tool's domain of interaction is closed.
		 * For example, the world of a web search tool is open, whereas that
		 * of a memory tool is not.
		 *
		 * Default: true
		 */
		boolean openWorldHint() default true;		
	}
}
