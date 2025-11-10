/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.server.runtime.AbstractFeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.PromptDefinition;
import za.co.sindi.ai.mcp.server.runtime.PromptDefinition.PromptArgumentInfo;

/**
 * @author Buhake Sindi
 * @since 25 April 2025
 */
public class DefaultPromptFeatureExecutor extends AbstractFeatureExecutor<GetPromptRequest,Prompt,PromptDefinition> {

	/**
	 * @param bean
	 * @param feature
	 */
	public DefaultPromptFeatureExecutor(BeanDefinition bean, PromptDefinition feature) {
		super(bean, feature);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Method findMethod(GetPromptRequest request) {
		// TODO Auto-generated method stub
		String name = request.getParameters().getName();
		if (name.equals(feature.getAnnotationName())) name = feature.getMethodName();
		for (Method method : bean.getBeanInstance().getInstanceType().getDeclaredMethods()) {
			if (method.getName().equals(name)) return method;
		}

		throw new IllegalArgumentException(String.format("Method '%s' is not found in object '%s'", name, bean.getBeanInstance().getInstanceType().getName()));
	}

	@Override
	protected Object[] prepareArguments(final Method method, GetPromptRequest request) {
		// TODO Auto-generated method stub
		Map<String, String> requestArguments = request.getParameters().getArguments();
		Parameter[] parameters = method.getParameters();
		Object[] parameterValues = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
			String parameterName = parameter.getName();
			PromptArgumentInfo promptArgument = feature.getArguments().stream().filter(argument -> argument.getParameterName().equals(parameterName)).findFirst().get();
			String requestArgumentValue = requestArguments.containsKey(parameterName) ? requestArguments.get(parameterName) : requestArguments.get(promptArgument.getAnnotationName());
			Class<?> parameterClass = parameter.getType();
            Type parameterType = parameter.getParameterizedType();

            parameterValues[i] = coerceArgument(requestArgumentValue, parameterName, parameterClass, parameterType);
		}
		return parameterValues;
	}
}
