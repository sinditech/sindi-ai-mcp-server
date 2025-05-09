/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.schema.Tool.InputSchema;
import za.co.sindi.ai.mcp.schema.Tool.InputSchema.PropertySchema;
import za.co.sindi.ai.mcp.server.runtime.impl.JSONDataTypes;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public class ToolDefinition implements FeatureDefinition<Tool>, Serializable {

	private Class<?> methodDeclaringClass;
	private String methodName;
	private Class<?> methodReturnType;
	
	private String annotationName;
	private String annotationDescription;
	
	private List<ToolArgumentInfo> arguments;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationName
	 * @param annotationDescription
	 * @param arguments
	 */
	public ToolDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType,
			String annotationName, String annotationDescription, List<ToolArgumentInfo> arguments) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationName = annotationName;
		this.annotationDescription = annotationDescription;
		this.arguments = arguments;
	}

	/**
	 * @return the methodDeclaringClass
	 */
	public Class<?> getMethodDeclaringClass() {
		return methodDeclaringClass;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the methodReturnType
	 */
	public Class<?> getMethodReturnType() {
		return methodReturnType;
	}

	/**
	 * @return the annotationName
	 */
	public String getAnnotationName() {
		return annotationName;
	}

	/**
	 * @return the annotationDescription
	 */
	public String getAnnotationDescription() {
		return annotationDescription;
	}

	/**
	 * @return the arguments
	 */
	public List<ToolArgumentInfo> getArguments() {
		return arguments;
	}

	@Override
	public Tool toMCPFeature() {
		// TODO Auto-generated method stub
		Tool tool = new Tool();
		tool.setName(Strings.isNullOrEmpty(annotationName) ? methodName : annotationName);
		tool.setDescription(annotationDescription);
		
		if (arguments != null) {
			InputSchema inputSchema = new InputSchema();
			inputSchema.setType("object");
			tool.setInputSchema(inputSchema);
			
			Map<String, PropertySchema> properties = new LinkedHashMap<>();
			List<String> requiredParams = new ArrayList<>();
			arguments.stream().forEach(argument -> {
				String name = Strings.isNullOrEmpty(argument.getAnnotationName()) ? argument.getParameterName() : argument.getAnnotationName();
				
				PropertySchema property = new PropertySchema();
				property.setType(JSONDataTypes.deterimineJsonType(argument.getParameterType())); // property.setType(argument.getParameterType().getName());
				property.setDescription(argument.getAnnotationDescription());
				
				properties.put(name, property);
				if (argument.isRequired()) requiredParams.add(name);
			});
			inputSchema.setProperties(properties);
			inputSchema.setRequired(requiredParams.toArray(new String[requiredParams.size()]));
			tool.setInputSchema(inputSchema);
		}
		
		return tool;
	}

	public static class ToolArgumentInfo implements Serializable {
		
		private Class<?> parameterType;
		private String parameterName;
		
		private String annotationName;
		private String annotationDescription;
		private boolean required;
		
		/**
		 * @param parameterType
		 * @param parameterName
		 * @param annotationName
		 * @param annotationDescription
		 * @param required
		 */
		public ToolArgumentInfo(Class<?> parameterType, String parameterName, String annotationName,
				String annotationDescription, boolean required) {
			super();
			this.parameterType = parameterType;
			this.parameterName = parameterName;
			this.annotationName = annotationName;
			this.annotationDescription = annotationDescription;
			this.required = required;
		}

		/**
		 * @return the parameterType
		 */
		public Class<?> getParameterType() {
			return parameterType;
		}

		/**
		 * @return the parameterName
		 */
		public String getParameterName() {
			return parameterName;
		}

		/**
		 * @return the annotationName
		 */
		public String getAnnotationName() {
			return annotationName;
		}

		/**
		 * @return the annotationDescription
		 */
		public String getAnnotationDescription() {
			return annotationDescription;
		}

		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return required;
		}
	}
}
