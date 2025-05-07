/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.CallToolResult;
import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesResult;
import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.schema.ReadResourceRequest;
import za.co.sindi.ai.mcp.schema.ReadResourceResult;
import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.PromptDefinition;
import za.co.sindi.ai.mcp.server.runtime.ResourceDefinition;
import za.co.sindi.ai.mcp.server.runtime.ResourceTemplatesDefinition;
import za.co.sindi.ai.mcp.server.runtime.ToolDefinition;
import za.co.sindi.ai.mcp.shared.RequestHandler;

/**
 * @author Buhake Sindi
 * @since 21 April 2025
 */
public class DefaultFeatureDefinitionManager implements FeatureDefinitionManager {
	
	//Definition registry
	private final Map<String, Entry<PromptDefinition, BeanDefinition>> prompts;
	private final Map<String, Entry<ToolDefinition, BeanDefinition>> tools;
	private final Map<String, Entry<ResourceDefinition, BeanDefinition>> resources;
	private final Map<String, Entry<ResourceTemplatesDefinition, BeanDefinition>> resourceTemplates;
	
	//Feature executor registry
	private final Map<String, FeatureExecutor<GetPromptRequest>> promptExecutors;
	private final Map<String, FeatureExecutor<CallToolRequest>> toolExecutors;
	private final Map<String, FeatureExecutor<ReadResourceRequest>> resourceExecutors;
	private final Map<String, FeatureExecutor<ListResourceTemplatesRequest>> resourceTemplatesExecutors;
	
	/**
	 * 
	 * @param beans
	 * @param factory
	 */
	public DefaultFeatureDefinitionManager(final List<BeanDefinition> beans, final FeatureExecutorFactory factory) {
		super();
		// TODO Auto-generated constructor stub
		prompts = beans.stream().flatMap(bean -> bean.getPrompts().stream().map(pd -> new SimpleEntry<>(pd.toMCPFeature().getName(), new SimpleEntry<>(pd, bean)))).collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue, (existing, duplicate) -> {   // Merge function that throws on duplicate
            throw new IllegalStateException("Duplicate prompt name found: " + duplicate.getKey().toMCPFeature().getName());
        }));
		tools = beans.stream().flatMap(bean -> bean.getTools().stream().map(td -> new SimpleEntry<>(td.toMCPFeature().getName(), new SimpleEntry<>(td, bean)))).collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue, (existing, duplicate) -> {   // Merge function that throws on duplicate
            throw new IllegalStateException("Duplicate tool name found: " + duplicate.getKey().toMCPFeature().getName());
        }));
		resources = beans.stream().flatMap(bean -> bean.getResources().stream().map(rd -> new SimpleEntry<>(rd.getAnnotationName(), new SimpleEntry<>(rd, bean)))).collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue, (existing, duplicate) -> {   // Merge function that throws on duplicate
            throw new IllegalStateException("Duplicate resource uri found: " + duplicate.getKey().toMCPFeature().getUri());
        }));
		resourceTemplates = beans.stream().flatMap(bean -> bean.getResourceTemplates().stream().map(rtd -> new SimpleEntry<>(rtd.getAnnotationUriTemplate(), new SimpleEntry<>(rtd, bean)))).collect(Collectors.toConcurrentMap(Entry::getKey, Entry::getValue, (existing, duplicate) -> {   // Merge function that throws on duplicate
            throw new IllegalStateException("Duplicate resource uri template found: " + duplicate.getKey().toMCPFeature().getUriTemplate());
        }));
		
		promptExecutors = prompts.entrySet().stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> factory.createPromptFeatureExecutor(entry.getValue().getValue(), entry.getValue().getKey())));
		toolExecutors = tools.entrySet().stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> factory.createToolFeatureExecutor(entry.getValue().getValue(), entry.getValue().getKey())));
		resourceExecutors = resources.entrySet().stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> factory.createResourceFeatureExecutor(entry.getValue().getValue(), entry.getValue().getKey())));
		resourceTemplatesExecutors = resourceTemplates.entrySet().stream().collect(Collectors.toConcurrentMap(Map.Entry::getKey, entry -> factory.createResourceTemplateFeatureExecutor(entry.getValue().getValue(), entry.getValue().getKey())));
	}

	@Override
	public List<Prompt> getPrompts() {
		// TODO Auto-generated method stub
		return prompts.values().stream().map(entry -> entry.getKey().toMCPFeature())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	@Override
	public RequestHandler<GetPromptResult> getPromptResultHandler(String promptName) {
		// TODO Auto-generated method stub
		return new DefaultPromptResultHandler(promptExecutors.get(promptName));
	}

	@Override
	public List<Tool> getTools() {
		// TODO Auto-generated method stub
		return tools.values().stream().map(entry -> entry.getKey().toMCPFeature()).toList();
                //.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	@Override
	public RequestHandler<CallToolResult> getToolResultHandler(String toolName) {
		// TODO Auto-generated method stub
		return new DefaultToolResultHandler(toolExecutors.get(toolName));
	}

	@Override
	public List<Resource> getResources() {
		// TODO Auto-generated method stub
		return resources.values().stream().map(entry -> entry.getKey().toMCPFeature()).toList();
                //.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	@Override
	public RequestHandler<ReadResourceResult> getResourceResultHandler(String resourceUri) {
		// TODO Auto-generated method stub
		return new DefaultResourceResultHandler(resourceExecutors.get(resourceUri));
	}

	@Override
	public List<ResourceTemplate> getResourceTemplates() {
		// TODO Auto-generated method stub
		return resourceTemplates.values().stream().map(entry -> entry.getKey().toMCPFeature()).toList();
               //.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

	@Override
	public RequestHandler<ListResourceTemplatesResult> getResourceTemplatesResultHandler(String resourceTemplateUri) {
		// TODO Auto-generated method stub
		return new DefaultResourceTemplatesResultHandler(resourceTemplatesExecutors.get(resourceTemplateUri));
	}
}
