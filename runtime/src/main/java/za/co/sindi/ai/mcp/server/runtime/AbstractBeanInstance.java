/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import za.co.sindi.commons.utils.Throwables;

/**
 * @author Buhake Sindi
 * @since 21 September 2025
 */
public abstract class AbstractBeanInstance implements BeanInstance {

	@Override
	public Object invoke(Object instance, Method method, Object... arguments) throws Throwable {
		// TODO Auto-generated method stub
		Object result = null;
        try {
            result = method.invoke(instance, arguments);
        } catch (IllegalAccessException e) {
        	method.setAccessible(true);
        	try {
        		result = method.invoke(instance, arguments);
        	} catch (InvocationTargetException ite) {
        		throw Throwables.getRootCause(ite);
        	}
        }
        return result;
	}
}
