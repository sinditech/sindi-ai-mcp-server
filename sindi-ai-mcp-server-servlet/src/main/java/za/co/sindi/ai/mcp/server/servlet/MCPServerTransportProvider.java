/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.ServerNotification;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 08 May 2025
 */
public interface MCPServerTransportProvider extends AutoCloseable {
	
	public MCPServerConfig getMCPServerConfig();

	public CompletableFuture<Void> notifyAllClients(final ServerNotification notification);
	
}
