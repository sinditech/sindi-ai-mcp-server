/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.List;

/**
 * @author Buhake Sindi
 * @since 13 September 2025
 */
public class ElicitDefinition implements FeatureDefinition<Object>, Serializable {

	private Class<?> methodDeclaringClass;
	private String methodName;
	private Class<?> methodReturnType;
	
	private String annotationMessage;
	
	private List<ElicitArgumentInfo> arguments;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationMessage
	 * @param arguments
	 */
	public ElicitDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType, String annotationMessage, List<ElicitArgumentInfo> arguments) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationMessage = annotationMessage;
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
	 * @return the annotationMessage
	 */
	public String getAnnotationMessage() {
		return annotationMessage;
	}

	/**
	 * @return the arguments
	 */
	public List<ElicitArgumentInfo> getArguments() {
		return arguments;
	}

	@Override
	public Object toMCPFeature() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class ElicitArgumentInfo implements Serializable {
		
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
		public ElicitArgumentInfo(Class<?> parameterType, String parameterName, String annotationName,
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
