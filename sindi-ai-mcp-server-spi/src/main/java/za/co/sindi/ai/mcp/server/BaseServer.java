/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.schema.ServerCapabilities.Builder;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 05 May 2025
 */
public class BaseServer extends Server {

	@Override
	public Implementation getServerInfo() {
		// TODO Auto-generated method stub
		final MCPServerConfig config = getMcpServerConfig();
		if (config == null) return null;
			
		return new Implementation(config.getServerName(), config.getServerVersion());
	}

	@Override
	public ServerCapabilities getServerCapabilities() {
		// TODO Auto-generated method stub
		final MCPServerConfig config = getMcpServerConfig();
		if (config == null) return null;
		
		Builder builder = new ServerCapabilities.Builder();
		if (config.shouldEnableCompletions())
			builder = builder.completions();
		
		if (config.shouldEnableLogging())
			builder = builder.logging();
		
		if (config.shouldEnablePrompts())
			builder = builder.prompts(config.notifyPromptListChanged());
			
		if (config.shouldEnableResources())
			builder = builder.resources(config.notifyResourceListChanged());
		
		if (config.shouldEnableTools()) 
			builder = builder.tools(config.notifyToolListChanged());
		
		return builder.build();
	}

	@Override
	public String getInstructions() {
		// TODO Auto-generated method stub
		final MCPServerConfig config = getMcpServerConfig();
		if (config == null) return null;
		
		return config.getInstructions();
	}

	public MCPServerConfig getMcpServerConfig() {
		throw new IllegalStateException("An MCP server config is required.");
	}
}
