/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.lang.reflect.Method;

import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.server.runtime.AbstractFeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.ResourceTemplatesDefinition;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultListResourceTemplatesFeatureExecutor extends AbstractFeatureExecutor<ListResourceTemplatesRequest,ResourceTemplate,ResourceTemplatesDefinition> {

	/**
	 * @param bean
	 * @param feature
	 */
	public DefaultListResourceTemplatesFeatureExecutor(BeanDefinition bean, ResourceTemplatesDefinition feature) {
		super(bean, feature);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Method findMethod(ListResourceTemplatesRequest request) {
		// TODO Auto-generated method stub
		String name = feature.getMethodName();
		for (Method method : bean.getBeanClass().getDeclaredMethods()) {
			if (method.getName().equals(name)) return method;
		}

		throw new IllegalArgumentException(String.format("Method '%s' is not found in object '%s'", name, bean.getBeanClass().getName()));
	}
}
