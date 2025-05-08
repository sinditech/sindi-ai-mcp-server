/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.ServerNotification;
import za.co.sindi.ai.mcp.server.BaseServer;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 08 May 2025
 */
public class HttpServletMCPServer extends BaseServer {

	private final MCPServerTransportProvider mcpServer;

	/**
	 * @param mcpServer
	 */
	public HttpServletMCPServer(MCPServerTransportProvider mcpServer) {
		super();
		this.mcpServer = Objects.requireNonNull(mcpServer, "An MCP Server Transport provider is required.");
	}

	@Override
	public MCPServerConfig getMcpServerConfig() {
		// TODO Auto-generated method stub
		return mcpServer.getMCPServerConfig();
	}

	@Override
	public CompletableFuture<Void> sendNotification(ServerNotification notification) {
		// TODO Auto-generated method stub
		return mcpServer.notifyAllClients(notification);
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		super.close();
		mcpServer.close();
	}
}
