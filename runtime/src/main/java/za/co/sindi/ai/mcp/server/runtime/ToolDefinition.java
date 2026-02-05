/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import za.co.sindi.ai.mcp.schema.Icon;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.schema.Tool.InputSchema;
import za.co.sindi.ai.mcp.schema.Tool.PropertySchema;
import za.co.sindi.ai.mcp.schema.Tool.ToolAnnotations;
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
	private String annotationTitle;
	private String annotationDescription;
	
	private List<IconInfo> icons;
	private ToolAnnotationsInfo annotations;
	private List<ToolArgumentInfo> arguments;
	
	/**
	 * @param methodDeclaringClass
	 * @param methodName
	 * @param methodReturnType
	 * @param annotationName
	 * @param annotationTitle
	 * @param annotationDescription
	 * @param icons;
	 * @param annotations
	 * @param arguments
	 */
	public ToolDefinition(Class<?> methodDeclaringClass, String methodName, Class<?> methodReturnType, String annotationName, 
			String annotationTitle, String annotationDescription, List<IconInfo> icons, ToolAnnotationsInfo annotations, List<ToolArgumentInfo> arguments) {
		super();
		this.methodDeclaringClass = methodDeclaringClass;
		this.methodName = methodName;
		this.methodReturnType = methodReturnType;
		this.annotationName = annotationName;
		this.annotationTitle = annotationTitle;
		this.annotationDescription = annotationDescription;
		this.icons = icons;
		this.annotations = annotations;
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
	 * @return the icons
	 */
	public List<IconInfo> getIcons() {
		return icons;
	}

	/**
	 * @return the annotations
	 */
	public ToolAnnotationsInfo getAnnotations() {
		return annotations;
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
		tool.setTitle(Strings.isNullOrEmpty(annotationTitle) ? null : annotationTitle);
		tool.setDescription(annotationDescription);
		
		if (icons != null && !icons.isEmpty()) {
			List<Icon> _icons = icons.stream().map(icon -> new Icon(Strings.isNullOrEmpty(icon.getSrc()) ? null : icon.getSrc(), Strings.isNullOrEmpty(icon.getMimeType()) ? null : icon.getMimeType(), icon.getSizes() == null || icon.getSizes().length == 0 ? null : icon.getSizes(), icon.getTheme())).toList();
			tool.setIcons(_icons.toArray(new Icon[_icons.size()]));
		}
		
		if (annotations != null) {
			tool.setAnnotations(new ToolAnnotations(Strings.isNullOrEmpty(annotations.getTitle()) ? null : annotations.getTitle(), annotations.isReadOnlyHint(), annotations.isDestructiveHint(), annotations.isIdempotentHint(), annotations.isOpenWorldHint()));
		}
		
		InputSchema inputSchema = new InputSchema();
		inputSchema.setType("object");
		tool.setInputSchema(inputSchema);
		
		if (arguments == null || arguments.isEmpty()) {
			inputSchema.setAdditionalProperties(false);
		} else {
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
	
	public static class ToolAnnotationsInfo implements Serializable {
		
		private String title;
		private boolean readOnlyHint;
		private boolean destructiveHint;
		private boolean idempotentHint;
		private boolean openWorldHint;
		
		/**
		 * @param title
		 * @param readOnlyHint
		 * @param destructiveHint
		 * @param idempotentHint
		 * @param openWorldHint
		 */
		public ToolAnnotationsInfo(String title, boolean readOnlyHint, boolean destructiveHint, boolean idempotentHint,
				boolean openWorldHint) {
			super();
			this.title = title;
			this.readOnlyHint = readOnlyHint;
			this.destructiveHint = destructiveHint;
			this.idempotentHint = idempotentHint;
			this.openWorldHint = openWorldHint;
		}

		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @return the readOnlyHint
		 */
		public boolean isReadOnlyHint() {
			return readOnlyHint;
		}

		/**
		 * @return the destructiveHint
		 */
		public boolean isDestructiveHint() {
			return destructiveHint;
		}

		/**
		 * @return the idempotentHint
		 */
		public boolean isIdempotentHint() {
			return idempotentHint;
		}

		/**
		 * @return the openWorldHint
		 */
		public boolean isOpenWorldHint() {
			return openWorldHint;
		}
	}
}
