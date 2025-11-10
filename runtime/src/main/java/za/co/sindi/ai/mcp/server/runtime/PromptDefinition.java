/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.List;

import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.schema.PromptArgument;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public class PromptDefinition implements FeatureDefinition<Prompt>, Serializable {

	private Class<?> methodDeclaringClass;
	private String methodName;
	private Class<?> methodReturnType;
	
	private String annotationName;
	private String annotationTitle;
	private String annotationDescription;
	
	private List<PromptArgumentInfo> arguments;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationName
	 * @param annotationTitle
	 * @param annotationDescription
	 * @param arguments
	 */
	public PromptDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType,
			String annotationName, String annotationTitle, String annotationDescription, List<PromptArgumentInfo> arguments) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationName = annotationName;
		this.annotationTitle = annotationTitle;
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
	 * @return the annotationTitle
	 */
	public String getAnnotationTitle() {
		return annotationTitle;
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
	public List<PromptArgumentInfo> getArguments() {
		return arguments;
	}

	@Override
	public Prompt toMCPFeature() {
		// TODO Auto-generated method stub
		Prompt prompt = new Prompt();
		prompt.setName(Strings.isNullOrEmpty(annotationName) ? methodName : annotationName);
		prompt.setTitle(Strings.isNullOrEmpty(annotationTitle) ? null : annotationTitle);
		prompt.setDescription(annotationDescription);
		
		if (arguments != null) {
			List<PromptArgument> promptArguments = arguments.stream().map(argument -> {
				PromptArgument pa = new PromptArgument(argument.getAnnotationName(), argument.getAnnotationTitle(), argument.getAnnotationDescription(), argument.isRequired());
				if (Strings.isNullOrEmpty(argument.getAnnotationName()))
					pa.setName(argument.getParameterName());
				return pa;
			}).toList();
			prompt.setArguments(promptArguments.toArray(new PromptArgument[promptArguments.size()]));
		}
		
		return prompt;
	}

	public static class PromptArgumentInfo implements Serializable {
		
		private Class<?> parameterType;
		private String parameterName;
		
		private String annotationName;
		private String annotationTitle;
		private String annotationDescription;
		private boolean required;
		
		/**
		 * @param parameterType
		 * @param parameterName
		 * @param annotationName
		 * @param annotationTitle
		 * @param annotationDescription
		 * @param required
		 */
		public PromptArgumentInfo(Class<?> parameterType, String parameterName, String annotationName, String annotationTitle,
				String annotationDescription, boolean required) {
			super();
			this.parameterType = parameterType;
			this.parameterName = parameterName;
			this.annotationName = annotationName;
			this.annotationTitle = annotationTitle;
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
		 * @return the annotationTitle
		 */
		public String getAnnotationTitle() {
			return annotationTitle;
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
