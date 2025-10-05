/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ReadResourceRequest;

/**
 * @author Buhake Sindi
 * @since 21 April 2025
 */
public interface FeatureExecutorFactory {

	public FeatureExecutor<GetPromptRequest> createPromptFeatureExecutor(final BeanDefinition bean, final PromptDefinition prompt);
	public FeatureExecutor<CallToolRequest> createToolFeatureExecutor(final BeanDefinition bean, final ToolDefinition tool);
	public FeatureExecutor<ReadResourceRequest> createResourceFeatureExecutor(final BeanDefinition bean, final ResourceDefinition resource);
	public FeatureExecutor<ListResourceTemplatesRequest> createResourceTemplateFeatureExecutor(final BeanDefinition bean, final ResourceTemplatesDefinition resourceTemplates);
}
