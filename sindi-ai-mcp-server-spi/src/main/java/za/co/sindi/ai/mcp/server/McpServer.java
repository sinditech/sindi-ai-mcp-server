/**
 * 
 */
package za.co.sindi.ai.mcp.server;

/**
 * @author Buhake Sindi
 * @since 02 October 2025
 */
public interface McpServer extends PromptManager, ResourceManager, ToolManager {

	public void setup(final MCPServerSession serverSession);
}
