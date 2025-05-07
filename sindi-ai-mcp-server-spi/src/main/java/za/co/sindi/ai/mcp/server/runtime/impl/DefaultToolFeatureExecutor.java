/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.server.runtime.AbstractFeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.ToolDefinition;
import za.co.sindi.ai.mcp.server.runtime.ToolDefinition.ToolArgumentInfo;

/**
 * @author Buhake Sindi
 * @since 25 April 2025
 */
public class DefaultToolFeatureExecutor extends AbstractFeatureExecutor<CallToolRequest,Tool,ToolDefinition> {

	/**
	 * @param bean
	 * @param feature
	 */
	public DefaultToolFeatureExecutor(BeanDefinition bean, ToolDefinition feature) {
		super(bean, feature);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Method findMethod(CallToolRequest request) {
		// TODO Auto-generated method stub
		String name = request.getParameters().getName();
		if (name.equals(feature.getAnnotationName())) name = feature.getMethodName();
		for (Method method : bean.getBeanClass().getDeclaredMethods()) {
			if (method.getName().equals(name)) return method;
		}

		throw new IllegalArgumentException(String.format("Method '%s' is not found in object '%s'", name, bean.getBeanClass().getName()));
	}

	@Override
	protected Object[] prepareArguments(final Method method, CallToolRequest request) {
		// TODO Auto-generated method stub
		Map<String, Object> requestArguments = request.getParameters().getArguments();
		Parameter[] parameters = method.getParameters();
		Object[] parameterValues = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
			String parameterName = parameter.getName();
			ToolArgumentInfo toolArgument = feature.getArguments().stream().filter(argument -> argument.getParameterName().equals(parameterName)).findFirst().get();
			Object requestArgumentValue = requestArguments.containsKey(parameterName) ? requestArguments.get(parameterName) : requestArguments.get(toolArgument.getAnnotationName());
			Class<?> parameterClass = parameter.getType();
            Type parameterType = parameter.getParameterizedType();

            parameterValues[i] = coerceArgument(requestArgumentValue, parameterName, parameterClass, parameterType);
		}
		return parameterValues;
	}
}
