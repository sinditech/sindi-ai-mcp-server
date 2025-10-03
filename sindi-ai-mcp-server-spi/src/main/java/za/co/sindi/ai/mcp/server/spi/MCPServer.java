package za.co.sindi.ai.mcp.server.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import za.co.sindi.ai.mcp.schema.LoggingLevel;

/**
 * @author Buhake Sindi
 * @since 02 September 2025
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface MCPServer {
	
	String name();
	String version();
	String instructions();
	boolean enableLogging() default false;
	boolean enableCompletions() default false;
	boolean enablePrompts() default false;
	boolean notifyPromptListChanged() default false;
	boolean enableResources() default false;
	boolean notifyResourceListChanged() default false;
	boolean enableTools() default false;
	boolean notifyToolListChanged() default false;
	LoggingLevel defaultLoggingLevel() default LoggingLevel.DEBUG;
	boolean enableAllFeatures() default false;
}
