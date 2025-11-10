/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.schema.LoggingLevel;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;
import za.co.sindi.ai.mcp.shared.ServerTransport;

/**
 * @author Buhake Sindi
 * @since 05 October 2025
 */
public interface MCPSession extends AutoCloseable {

	public String getId();
	
	public LoggingLevel getLoggingLevel();
	
	public MCPLogger getLogger();
	
	public ServerTransport getTransport();
	
	public void closeQuietly();
}
