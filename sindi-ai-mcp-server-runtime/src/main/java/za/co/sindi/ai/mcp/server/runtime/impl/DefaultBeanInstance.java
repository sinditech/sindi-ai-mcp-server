/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.server.runtime.AbstractBeanInstance;

/**
 * @author Buhake Sindi
 * @since 21 September 2025
 */
public class DefaultBeanInstance extends AbstractBeanInstance {

	private final Class<?> instanceType;
	private final Object instance;
	
	/**
	 * @param instance
	 */
	public DefaultBeanInstance(Class<?> instanceType) throws ReflectiveOperationException{
		this(instanceType, instanceType.getDeclaredConstructor().newInstance());
	}
	
	/**
	 * @param instance
	 */
	public DefaultBeanInstance(Object instance) {
		this(instance.getClass(), instance);
	}

	/**
	 * @param instanceType
	 * @param instance
	 */
	public DefaultBeanInstance(Class<?> instanceType, Object instance) {
		super();
		this.instanceType = instanceType;
		this.instance = instance;
	}

	@Override
	public Object getInstance() {
		// TODO Auto-generated method stub
		return instance;
	}

	@Override
	public Class<?> getInstanceType() {
		// TODO Auto-generated method stub
		return instanceType;
	}
}
