/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.List;

import za.co.sindi.ai.mcp.schema.Annotations;
import za.co.sindi.ai.mcp.schema.Icon;
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
	private String annotationTitle;
	private String annotationDescription;
	private String annotationMimeType;
	
	private List<IconInfo> icons;
	private AnnotationsInfo annotations;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationUriTemplate
	 * @param annotationName
	 * @param annotationTitle
	 * @param annotationDescription
	 * @param annotationMimeType
	 * @param icons
	 * @param annotations
	 */
	public ResourceTemplatesDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType, String annotationUriTemplate, 
			String annotationName, String annotationTitle, String annotationDescription, String annotationMimeType, List<IconInfo> icons, AnnotationsInfo annotations) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationUriTemplate = annotationUriTemplate;
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
	public ResourceTemplate toMCPFeature() {
		// TODO Auto-generated method stub
		ResourceTemplate resourceTemplate = new ResourceTemplate();
		resourceTemplate.setUriTemplate(annotationUriTemplate);
		resourceTemplate.setName(annotationName);
		if (Strings.isNullOrEmpty(annotationName)) resourceTemplate.setName(methodName);
		resourceTemplate.setDescription(annotationDescription);
		resourceTemplate.setMimeType(annotationMimeType);
		resourceTemplate.setTitle(Strings.isNullOrEmpty(annotationTitle) ? null : annotationTitle);
		
		if (icons != null && !icons.isEmpty()) {
			List<Icon> _icons = icons.stream().map(icon -> new Icon(Strings.isNullOrEmpty(icon.getSrc()) ? null : icon.getSrc(), Strings.isNullOrEmpty(icon.getMimeType()) ? null : icon.getMimeType(), icon.getSizes() == null || icon.getSizes().length == 0 ? null : icon.getSizes(), icon.getTheme())).toList();
			resourceTemplate.setIcons(_icons.toArray(new Icon[_icons.size()]));
		}
		
		if (annotations != null) {
			resourceTemplate.setAnnotations(new Annotations(annotations.getAudience() == null || annotations.getAudience().length == 0 ? null : annotations.getAudience(), null, null));
		}
		
		return resourceTemplate;
	}
}
