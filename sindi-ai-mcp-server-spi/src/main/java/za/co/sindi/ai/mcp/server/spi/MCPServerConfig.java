/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public interface MCPServerConfig {
	
	public Implementation getServerInfo();
	
	public ServerCapabilities getCapabilities();

	public String getInstructions();
	
	public MCPServerConfig enableAll();
}
