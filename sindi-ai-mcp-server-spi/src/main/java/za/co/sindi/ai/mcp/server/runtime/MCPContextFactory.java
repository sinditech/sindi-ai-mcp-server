/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 16 September 2025
 */
public interface MCPContextFactory {

	public MCPContext getMCPContext(final MCPServerConfig serverConfig, final ResourceManager resourceManager, final PromptManager promptManager, final ToolManager toolManager, final MCPServerSession currentServerSession);
}
