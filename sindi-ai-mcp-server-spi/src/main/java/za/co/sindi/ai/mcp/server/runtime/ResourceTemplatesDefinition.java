/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;

import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public class ResourceTemplatesDefinition implements FeatureDefinition<ResourceTemplate>, Serializable {

	private Class<?> methodDeclaringClass;
	private String methodName;
	private Class<?> methodReturnType;
	
	private String annotationUriTemplate;
	private String annotationName;
	private String annotationDescription;
	private String annotationMimeType;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationUriTemplate
	 * @param annotationName
	 * @param annotationDescription
	 * @param annotationMimeType
	 */
	public ResourceTemplatesDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType,
			String annotationUriTemplate, String annotationName, String annotationDescription, String annotationMimeType) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationUriTemplate = annotationUriTemplate;
		this.annotationName = annotationName;
		this.annotationDescription = annotationDescription;
		this.annotationMimeType = annotationMimeType;
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
	 * @return the annotationUriTemplate
	 */
	public String getAnnotationUriTemplate() {
		return annotationUriTemplate;
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
	 * @return the annotationMimeType
	 */
	public String getAnnotationMimeType() {
		return annotationMimeType;
	}

	@Override
	public ResourceTemplate toMCPFeature() {
		// TODO Auto-generated method stub
		ResourceTemplate resourceTemplate = new ResourceTemplate();
		resourceTemplate.setUriTemplate(annotationUriTemplate);
		resourceTemplate.setName(annotationName);
		if (Strings.isNullOrEmpty(annotationName)) resourceTemplate.setName(methodName);
		resourceTemplate.setDescription(annotationDescription);
		resourceTemplate.setMimeType(annotationMimeType);
		
		return resourceTemplate;
	}
}
