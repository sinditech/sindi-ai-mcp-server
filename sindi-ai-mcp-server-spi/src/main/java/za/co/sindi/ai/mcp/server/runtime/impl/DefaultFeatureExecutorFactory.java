/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ReadResourceRequest;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.PromptDefinition;
import za.co.sindi.ai.mcp.server.runtime.ResourceDefinition;
import za.co.sindi.ai.mcp.server.runtime.ResourceTemplatesDefinition;
import za.co.sindi.ai.mcp.server.runtime.ToolDefinition;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultFeatureExecutorFactory implements FeatureExecutorFactory {

	@Override
	public FeatureExecutor<GetPromptRequest> createPromptFeatureExecutor(BeanDefinition bean, PromptDefinition prompt) {
		// TODO Auto-generated method stub
		return new DefaultPromptFeatureExecutor(bean, prompt);
	}

	@Override
	public FeatureExecutor<CallToolRequest> createToolFeatureExecutor(BeanDefinition bean, ToolDefinition tool) {
		// TODO Auto-generated method stub
		return new DefaultToolFeatureExecutor(bean, tool);
	}

	@Override
	public FeatureExecutor<ReadResourceRequest> createResourceFeatureExecutor(BeanDefinition bean, ResourceDefinition resource) {
		// TODO Auto-generated method stub
		return new DefaultResourceFeatureExecutor(bean, resource);
	}

	@Override
	public FeatureExecutor<ListResourceTemplatesRequest> createResourceTemplateFeatureExecutor(BeanDefinition bean,	ResourceTemplatesDefinition resourceTemplates) {
		// TODO Auto-generated method stub
		return new DefaultResourceTemplatesFeatureExecutor(bean, resourceTemplates);
	}
}
