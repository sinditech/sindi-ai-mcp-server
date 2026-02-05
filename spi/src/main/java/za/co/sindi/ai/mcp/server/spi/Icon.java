package za.co.sindi.ai.mcp.server.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import za.co.sindi.ai.mcp.schema.Icon.Theme;

/**
 * @author Buhake Sindi
 * @since 11 January 2026
 */
@Documented
@Retention(RUNTIME)
public @interface Icon {
	
	String src();
	
	String mimeType() default "";
	
	String[] sizes() default {};
	
	Theme theme();
}
