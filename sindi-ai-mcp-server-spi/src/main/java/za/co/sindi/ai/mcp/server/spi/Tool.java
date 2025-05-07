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
     * @return name of the tool.
     */
	String name() default "";
	
	/**
     * Description of the tool.
     * It should be clear and descriptive to allow language model to understand the tool's purpose and its intended use.
     *
     * @return description of the tool.
     */
	String description() default "";
}
