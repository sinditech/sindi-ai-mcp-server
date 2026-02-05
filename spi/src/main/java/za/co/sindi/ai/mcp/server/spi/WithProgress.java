package za.co.sindi.ai.mcp.server.spi;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * @author Buhake Sindi
 * @since 01 February 2026
 */
@Documented
@Qualifier
@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface WithProgress {
	
	 public static final class Literal extends AnnotationLiteral<WithProgress> implements WithProgress {
        /** The default WithProgress literal */
        public static final Literal INSTANCE = new Literal();

        private static final long serialVersionUID = 1L;

    }
}
