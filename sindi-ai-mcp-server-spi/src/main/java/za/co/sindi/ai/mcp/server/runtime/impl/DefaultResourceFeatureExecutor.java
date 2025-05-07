/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.lang.reflect.Method;

import za.co.sindi.ai.mcp.schema.ReadResourceRequest;
import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.ai.mcp.server.runtime.AbstractFeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.ResourceDefinition;

/**
 * @author Buhake Sindi
 * @since 25 April 2025
 */
public class DefaultResourceFeatureExecutor extends AbstractFeatureExecutor<ReadResourceRequest,Resource,ResourceDefinition> {

	/**
	 * @param bean
	 * @param feature
	 */
	public DefaultResourceFeatureExecutor(BeanDefinition bean, ResourceDefinition feature) {
		super(bean, feature);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Method findMethod(ReadResourceRequest request) {
		// TODO Auto-generated method stub
		String name = feature.getMethodName();
		for (Method method : bean.getBeanClass().getDeclaredMethods()) {
			if (method.getName().equals(name)) return method;
		}

		throw new IllegalArgumentException(String.format("Method '%s' is not found in object '%s'", name, bean.getBeanClass().getName()));
	}
}
