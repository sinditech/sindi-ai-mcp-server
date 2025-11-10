/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ToolManager;

/**
 * @author Buhake Sindi
 * @since 02 October 2025
 */
public interface MCPServer extends PromptManager, ResourceManager, ToolManager {

	public void setup(final Server server);
}
