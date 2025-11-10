/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.util.List;

import za.co.sindi.ai.mcp.schema.CallToolResult;
import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesResult;
import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.schema.ReadResourceResult;
import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.shared.RequestHandler;

/**
 * @author Buhake Sindi
 * @since 16 April 2025
 */
public interface FeatureDefinitionManager {
	
	public List<Prompt> getPrompts();
	public RequestHandler<GetPromptResult> getPromptResultHandler(final String promptName);
	
	public List<Tool> getTools();
	public RequestHandler<CallToolResult> getToolResultHandler(final String toolName);
	
	public List<Resource> getResources();
	public RequestHandler<ReadResourceResult> getResourceResultHandler(final String resourceUri);
	
	public List<ResourceTemplate> getResourceTemplates();
	public RequestHandler<ListResourceTemplatesResult> getResourceTemplatesResultHandler(final String resourceTemplateUri);

}
