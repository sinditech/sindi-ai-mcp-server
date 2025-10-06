/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestParameters;
import za.co.sindi.ai.mcp.schema.ElicitResult;
import za.co.sindi.ai.mcp.schema.Implementation;
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
	
	private static ThreadLocal<MCPContext> instance = new ThreadLocal<>();
	
	public static MCPContext getCurrentInstance() {
		return instance.get();
	}
	
	protected static void setCurrentInstance(MCPContext context) {
		if (context == null) {
            instance.remove();
        } else {
            instance.set(context);
        }
	}

	public abstract Implementation getServerInfo();
	
	public abstract ServerCapabilities getServerCapabilities();
	
//	public abstract FeatureManager getFeatureManager();
	
	public abstract ResourceManager getResourceManager();
	
	public abstract PromptManager getPromptManager();
	
	public abstract ToolManager getToolManager();
	
	public abstract RootsProvider getRootsProvider();
	
	public abstract MCPLogger getCurrentLogger();
	
	public abstract CompletableFuture<ElicitResult> elicitInput(final ElicitRequestParameters requestParameters);
	
	public abstract void release();
}
