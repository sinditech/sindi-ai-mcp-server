/**
 * @author Buhake Sindi
 * @since 16 September 2025
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.MCPContext;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * 
 */
public class DefaultMCPContextFactory implements MCPContextFactory {

	@Override
	public MCPContext getMCPContext(MCPServerConfig serverConfig, ResourceManager resourceManager,
			PromptManager promptManager, ToolManager toolManager, MCPServerSession currentServerSession) {
		// TODO Auto-generated method stub
		return new DefaultMCPContext(serverConfig, promptManager, resourceManager, toolManager, currentServerSession);
	}
}
