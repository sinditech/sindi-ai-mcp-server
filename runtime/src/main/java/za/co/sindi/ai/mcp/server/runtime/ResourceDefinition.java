/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.List;

import za.co.sindi.ai.mcp.schema.Annotations;
import za.co.sindi.ai.mcp.schema.Icon;
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
	
	private List<IconInfo> icons;
	private AnnotationsInfo annotations;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationUri
	 * @param annotationName
	 * @param annotationTitle
	 * @param annotationDescription
	 * @param annotationMimeType
	 * @param icons;
	 * @param annotations
	 */
	public ResourceDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType, String annotationUri, 
			String annotationName, String annotationTitle, String annotationDescription, String annotationMimeType, List<IconInfo> icons, AnnotationsInfo annotations) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationUri = annotationUri;
		this.annotationName = annotationName;
		this.annotationTitle = annotationTitle;
		this.annotationDescription = annotationDescription;
		this.annotationMimeType = annotationMimeType;
		this.icons = icons;
		this.annotations = annotations;
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
	
	/**
	 * @return the icons
	 */
	public List<IconInfo> getIcons() {
		return icons;
	}

	/**
	 * @return the annotations
	 */
	public AnnotationsInfo getAnnotations() {
		return annotations;
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
		
		if (icons != null && !icons.isEmpty()) {
			List<Icon> _icons = icons.stream().map(icon -> new Icon(Strings.isNullOrEmpty(icon.getSrc()) ? null : icon.getSrc(), Strings.isNullOrEmpty(icon.getMimeType()) ? null : icon.getMimeType(), icon.getSizes() == null || icon.getSizes().length == 0 ? null : icon.getSizes(), icon.getTheme())).toList();
			resource.setIcons(_icons.toArray(new Icon[_icons.size()]));
		}
		
		if (annotations != null) {
			resource.setAnnotations(new Annotations(annotations.getAudience() == null || annotations.getAudience().length == 0 ? null : annotations.getAudience(), null, null));
		}
		
		return resource;
	}
}
