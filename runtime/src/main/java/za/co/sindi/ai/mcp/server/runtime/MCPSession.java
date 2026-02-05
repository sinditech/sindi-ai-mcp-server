/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.schema.LoggingLevel;
import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.schema.RequestMeta;
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
	
	public void create(final RequestId requestId, final RequestMeta meta);
	
	public RequestContext get(final RequestId requestId);
	
	public void remove(final RequestId requestId);
}
