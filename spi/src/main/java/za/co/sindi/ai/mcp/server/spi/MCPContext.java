/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.schema.RequestMeta;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.ToolManager;

/**
 * @author Buhake Sindi
 * @since 21 April 2025
 */
public abstract class MCPContext {

	private static volatile MCPContext instance;

	public static MCPContext getCurrentInstance() {
		return instance;
	}

	protected static void setCurrentInstance(MCPContext context) {
		instance = context;
	}

	public abstract Implementation getServerInfo();

	public abstract ServerCapabilities getServerCapabilities();

	public abstract ResourceManager getResourceManager();

	public abstract PromptManager getPromptManager();

	public abstract ToolManager getToolManager();

	public abstract RootsProvider getRootsProvider();

	public abstract MCPLogger getCurrentLogger();

	public abstract CancellationContext getCancellationContext();

	public abstract ElicitationContext getElicitationContext();

	public abstract ProgressContext getProgressContext();

	public abstract void release();

	public abstract void setCurrentRequest(final RequestId requestId, final RequestMeta meta);

	public abstract void cancelRequest(final RequestId requestId, final String reason);
}
