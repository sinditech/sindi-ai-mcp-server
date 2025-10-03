/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.lang.reflect.Method;

/**
 * @author Buhake Sindi
 * @since 21 September 2025
 */
public interface BeanInstance {

	Object getInstance();

    Class<?> getInstanceType();
    
    default Object invoke(final Method method, Object... arguments) throws Throwable {
    	return invoke(getInstance(), method, arguments);
    }

    Object invoke(final Object instance, final Method method, Object... arguments) throws Throwable;
}
