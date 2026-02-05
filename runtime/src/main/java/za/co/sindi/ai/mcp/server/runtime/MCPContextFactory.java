/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 16 September 2025
 */
public interface MCPContextFactory {

	public MCPContext getMCPContext(final MCPServerConfig serverConfig, final MCPServer mcpServer/*, final MCPSession session */);
	
}
