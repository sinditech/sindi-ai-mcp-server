/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.schema.CallToolResult;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.shared.RequestHandler;

/**
 * @author Buhake Sindi
 * @since 01 May 2025
 */
public interface ToolManager {

	public void addTool(final Tool tool, final RequestHandler<CallToolResult> handler);
	public void removeTool(final String toolName);
}
