/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import za.co.sindi.ai.mcp.server.runtime.AbstractBeanInstance;

/**
 * @author Buhake Sindi
 * @since 21 September 2025
 */
public class ContextualBeanInstance extends AbstractBeanInstance {
	
	private final Class<?> instanceType;
	private final BeanManager beanManager;

	/**
	 * @param instanceType
	 * @param beanManager
	 */
	public ContextualBeanInstance(Class<?> instanceType, BeanManager beanManager) {
		super();
		this.instanceType = instanceType;
		this.beanManager = beanManager;
	}

	@Override
	public Object getInstance() {
		// TODO Auto-generated method stub
		Bean<?> bean  = beanManager.resolve(beanManager.getBeans(instanceType));
		CreationalContext<?> context = beanManager.createCreationalContext(bean);
		return beanManager.getReference(bean, instanceType, context);
	}

	@Override
	public Class<?> getInstanceType() {
		// TODO Auto-generated method stub
		return instanceType;
	}
}
