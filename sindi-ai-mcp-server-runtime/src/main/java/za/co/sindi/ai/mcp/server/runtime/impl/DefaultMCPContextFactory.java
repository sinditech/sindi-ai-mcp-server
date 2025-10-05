/**
 * @author Buhake Sindi
 * @since 16 September 2025
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.server.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.MCPContext;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * 
 */
public class DefaultMCPContextFactory implements MCPContextFactory {

	@Override
	public MCPContext getMCPContext(MCPServerConfig serverConfig, MCPServer mcpServer, MCPSession session) {
		// TODO Auto-generated method stub
		return new DefaultMCPContext(serverConfig, mcpServer, session);
	}
}
