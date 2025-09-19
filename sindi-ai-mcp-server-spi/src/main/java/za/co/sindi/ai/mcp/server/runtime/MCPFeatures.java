/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import za.co.sindi.ai.mcp.server.runtime.ElicitDefinition.ElicitArgumentInfo;
import za.co.sindi.ai.mcp.server.runtime.PromptDefinition.PromptArgumentInfo;
import za.co.sindi.ai.mcp.server.runtime.ToolDefinition.ToolArgumentInfo;
import za.co.sindi.ai.mcp.server.spi.Elicit;
import za.co.sindi.ai.mcp.server.spi.ElicitArgument;
import za.co.sindi.ai.mcp.server.spi.Prompt;
import za.co.sindi.ai.mcp.server.spi.PromptArgument;
import za.co.sindi.ai.mcp.server.spi.Resource;
import za.co.sindi.ai.mcp.server.spi.ResourceTemplate;
import za.co.sindi.ai.mcp.server.spi.Tool;
import za.co.sindi.ai.mcp.server.spi.ToolArgument;
import za.co.sindi.commons.utils.Annotations;

/**
 * @author Buhake Sindi
 * @since 20 April 2025
 */
@SuppressWarnings("unchecked")
public class MCPFeatures {
	
	private static final Logger LOGGER = Logger.getLogger(MCPFeatures.class.getName()); 
	
	private static final String LANGCHAIN4J_TOOL_CLASS_NAME = "dev.langchain4j.agent.tool.Tool";
	private static final Class<? extends Annotation> LANGCHAIN4J_TOOL_ANNOTATION_CLASS;
	private static final String LANGCHAIN4J_TOOL_P_CLASS_NAME = "dev.langchain4j.agent.tool.P";
	private static final Class<? extends Annotation> LANGCHAIN4J_TOOL_P_ANNOTATION_CLASS;
	
	public static final Class<? extends Annotation>[] MCP_METHOD_ANNOTATIONS;
	
	static {
		List<Class<? extends Annotation>> annotations = new ArrayList<>(List.of(Tool.class, Prompt.class, Resource.class, ResourceTemplate.class, Elicit.class));
		Class<? extends Annotation> clazz = null;
		try {
			clazz = (Class<? extends Annotation>) Class.forName(LANGCHAIN4J_TOOL_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		}
		LANGCHAIN4J_TOOL_ANNOTATION_CLASS = clazz;
		
		if (LANGCHAIN4J_TOOL_ANNOTATION_CLASS != null) annotations.add(LANGCHAIN4J_TOOL_ANNOTATION_CLASS);
		MCP_METHOD_ANNOTATIONS = annotations.toArray(new Class[annotations.size()]);
		
		clazz = null;
		try {
			clazz = (Class<? extends Annotation>) Class.forName(LANGCHAIN4J_TOOL_P_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		}
		LANGCHAIN4J_TOOL_P_ANNOTATION_CLASS = clazz;
	}
	
	public static BeanDefinition createBeanDefinition(final Class<?> clazz, final Object instance) {
		final List<ResourceDefinition> resources = new ArrayList<>();
		final List<ResourceTemplatesDefinition> resourceTemplates = new ArrayList<>();
		final List<ToolDefinition> tools = new ArrayList<>();
		final List<PromptDefinition> prompts = new ArrayList<>();
		final List<ElicitDefinition> elicitations = new ArrayList<>();
		
		extractMCPDefinition(clazz, tools, prompts, resources, resourceTemplates, elicitations);
		if (tools.isEmpty() &&
			prompts.isEmpty() && 
			resources.isEmpty() && 
			resourceTemplates.isEmpty() &&
			elicitations.isEmpty()) {
			return null;
		}
		
		return new BeanDefinition(clazz, instance, resources, resourceTemplates, tools, prompts, elicitations);
	}
	
	private static void extractMCPDefinition(final Class<?> clazz, List<ToolDefinition> tools, List<PromptDefinition> prompts, List<ResourceDefinition> resources, List<ResourceTemplatesDefinition> resourceTemplates, List<ElicitDefinition> elicitations) {
		try {
			for (Method declaredMethod : clazz.getDeclaredMethods()) {
				ToolDefinition tool = createToolDefinition(declaredMethod);
				if (tool != null) tools.add(tool);
				tool = createToolDefinitionFromLangChain4J(declaredMethod);
				if (tool != null) tools.add(tool);
				PromptDefinition prompt = createPromptDefinition(declaredMethod);
				if (prompt != null) prompts.add(prompt);
				ResourceDefinition resource = createResourceDefinition(declaredMethod);
				if (resource != null) resources.add(resource);
				ResourceTemplatesDefinition resourceTemplatesDef = createResourceTemplatesDefinition(declaredMethod);
				if (resourceTemplatesDef != null) resourceTemplates.add(resourceTemplatesDef);
				ElicitDefinition elicit = createElicitDefinition(declaredMethod);
				if (elicit != null) elicitations.add(elicit);
			}
		} catch (SecurityException | ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.WARNING, "Skipping class " + clazz.getName() + " due to the following issue:", e);
		}
	}
	
	private static ToolDefinition createToolDefinition(final Method method) {
		ToolDefinition toolDefinition = null;
		Tool tool = method.getAnnotation(Tool.class);
		if (tool != null) {
			toolDefinition = new ToolDefinition(method.getDeclaringClass(), method.getName(), method.getReturnType(), tool.name(), tool.title(), tool.description(), createToolArgumentInfo(method));
		}
		return toolDefinition;
	}
	
	private static List<ToolArgumentInfo> createToolArgumentInfo(final Method method) {
		List<ToolArgumentInfo> parameters = null;
		int parameterCount = method.getParameterCount();
		if (parameterCount > 0) {
			parameters = new ArrayList<>();
			for (int index = 0; index < parameterCount; index++) {
				Parameter parameter = method.getParameters()[index];
				ToolArgument argument = parameter.getAnnotation(ToolArgument.class);
				ToolArgumentInfo parameterDefinition = new ToolArgumentInfo(method.getParameterTypes()[index], parameter.getName(), argument.name(), argument.description(), argument.required());
				parameters.add(parameterDefinition);
			}
		}
		
		return parameters;
	}
	
	private static ToolDefinition createToolDefinitionFromLangChain4J(final Method method) throws ReflectiveOperationException {
		ToolDefinition toolDefinition = null;
		if (LANGCHAIN4J_TOOL_ANNOTATION_CLASS != null) {
			Annotation langchain4JTool = method.getAnnotation(LANGCHAIN4J_TOOL_ANNOTATION_CLASS);
			if (langchain4JTool != null) {
				toolDefinition = new ToolDefinition(method.getDeclaringClass(), method.getName(), method.getReturnType(), Annotations.getAnnotationValue(langchain4JTool, "name"), null, Annotations.getAnnotationValue(langchain4JTool, "value"), createToolArgumentInfoFromLangChain4J(method));
			}
		}
		return toolDefinition;
	}
	
	private static List<ToolArgumentInfo> createToolArgumentInfoFromLangChain4J(final Method method) throws ReflectiveOperationException {
		List<ToolArgumentInfo> parameters = null;
		if (LANGCHAIN4J_TOOL_P_ANNOTATION_CLASS != null) {
			int parameterCount = method.getParameterCount();
			if (parameterCount > 0) {
				parameters = new ArrayList<>();
				for (int index = 0; index < parameterCount; index++) {
					Parameter parameter = method.getParameters()[index];
					Annotation toolP = parameter.getAnnotation(LANGCHAIN4J_TOOL_P_ANNOTATION_CLASS);
					ToolArgumentInfo  parameterDefinition = new ToolArgumentInfo(method.getParameterTypes()[index], parameter.getName(), null, Annotations.getAnnotationValue(toolP, "value"), Annotations.getAnnotationValue(toolP, "required"));
					parameters.add(parameterDefinition);
				}
			}
		}
		
		return parameters;
	}
	
	private static PromptDefinition createPromptDefinition(final Method method) {
		PromptDefinition promptDefinition = null;
		Prompt prompt = method.getAnnotation(Prompt.class);
		if (prompt != null) {
			promptDefinition = new PromptDefinition(method.getDeclaringClass(), method.getName(), method.getReturnType(), prompt.name(), prompt.title(), prompt.description(), createPromptArgumenInfo(method));
		}
		return promptDefinition;
	}
	
	private static List<PromptArgumentInfo> createPromptArgumenInfo(final Method method) {
		List<PromptArgumentInfo> parameters = null;
		int parameterCount = method.getParameterCount();
		if (parameterCount > 0) {
			parameters = new ArrayList<>();
			for (int index = 0; index < parameterCount; index++) {
				Parameter parameter = method.getParameters()[index];
				PromptArgument argument = parameter.getAnnotation(PromptArgument.class);
				PromptArgumentInfo parameterDefinition = new PromptArgumentInfo(method.getParameterTypes()[index], parameter.getName(), argument.name(), argument.title(), argument.description(), argument.required());
				parameters.add(parameterDefinition);
			}
		}
		
		return parameters;
	}
	
	private static ResourceDefinition createResourceDefinition(final Method method) {
		ResourceDefinition resourceDefinition = null;
		Resource resource = method.getAnnotation(Resource.class);
		if (resource != null) {
			resourceDefinition = new ResourceDefinition(method.getDeclaringClass(), method.getName(), method.getReturnType(), resource.uri(), resource.name(), resource.title(), resource.description(), resource.mimeType());
		}
		return resourceDefinition;
	}
	
	private static ResourceTemplatesDefinition createResourceTemplatesDefinition(final Method method) {
		ResourceTemplatesDefinition resourceTemplatesDefinition = null;
		ResourceTemplate resourceTemplate = method.getAnnotation(ResourceTemplate.class);
		if (resourceTemplate != null) {
			resourceTemplatesDefinition = new ResourceTemplatesDefinition(method.getDeclaringClass(), method.getName(), method.getReturnType(), resourceTemplate.uri(), resourceTemplate.name(), resourceTemplate.title(), resourceTemplate.description(), resourceTemplate.mimeType());
		}
		return resourceTemplatesDefinition;
	}
	
	private static ElicitDefinition createElicitDefinition(final Method method) {
		ElicitDefinition elicitDefinition = null;
		Elicit elicit = method.getAnnotation(Elicit.class);
		if (elicit != null) {
			elicitDefinition = new ElicitDefinition(method.getDeclaringClass(), method.getName(), method.getReturnType(), elicit.message(), createElicitArgumentInfo(method));
		}
		return elicitDefinition;
	}
	
	private static List<ElicitArgumentInfo> createElicitArgumentInfo(final Method method) {
		List<ElicitArgumentInfo> parameters = null;
		int parameterCount = method.getParameterCount();
		if (parameterCount > 0) {
			parameters = new ArrayList<>();
			for (int index = 0; index < parameterCount; index++) {
				Parameter parameter = method.getParameters()[index];
				ElicitArgument argument = parameter.getAnnotation(ElicitArgument.class);
				ElicitArgumentInfo parameterDefinition = new ElicitArgumentInfo(method.getParameterTypes()[index], parameter.getName(), argument.name(), argument.description(), argument.required());
				parameters.add(parameterDefinition);
			}
		}
		
		return parameters;
	}
}
