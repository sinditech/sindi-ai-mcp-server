/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;

import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public class ResourceDefinition implements FeatureDefinition<Resource>, Serializable {

	private Class<?> methodDeclaringClass;
	private String methodName;
	private Class<?> methodReturnType;
	
	private String annotationUri;
	private String annotationName;
	private String annotationTitle;
	private String annotationDescription;
	private String annotationMimeType;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationUri
	 * @param annotationName
	 * @param annotationTitle
	 * @param annotationDescription
	 * @param annotationMimeType
	 */
	public ResourceDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType,
			String annotationUri, String annotationName, String annotationTitle, String annotationDescription, String annotationMimeType) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationUri = annotationUri;
		this.annotationName = annotationName;
		this.annotationTitle = annotationTitle;
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
	 * @return the annotationUri
	 */
	public String getAnnotationUri() {
		return annotationUri;
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
	 * @return the annotationMimeType
	 */
	public String getAnnotationMimeType() {
		return annotationMimeType;
	}
	
	@Override
	public Resource toMCPFeature() {
		// TODO Auto-generated method stub
		Resource resource = new Resource();
		resource.setUri(annotationUri);
		resource.setName(annotationName);
		if (Strings.isNullOrEmpty(annotationName)) resource.setName(methodName);
		resource.setDescription(annotationDescription);
		resource.setMimeType(annotationMimeType);
		resource.setTitle(Strings.isNullOrEmpty(annotationTitle) ? null : annotationTitle);
		
		return resource;
	}
}
