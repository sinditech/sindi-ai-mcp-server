/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.schema.RequestMeta;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.RequestContext;
import za.co.sindi.ai.mcp.server.spi.CancellationContext;
import za.co.sindi.ai.mcp.server.spi.ElicitationContext;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.ai.mcp.server.spi.ProgressContext;

/**
 * @author Buhake Sindi
 * @since 23 April 2025
 */
public class DefaultMCPContext extends MCPContext {

	private MCPServerConfig serverConfig;
	private MCPServer mcpServer;
	private MCPSession currentSession;
	private RequestContext currentRequest;
	
	/**
	 * @param serverConfig
	 * @param mcpServer
	 */
	public DefaultMCPContext(MCPServerConfig serverConfig, MCPServer mcpServer/*, MCPSession currentSession*/) {
		this.serverConfig = serverConfig;
		this.mcpServer = mcpServer;
		setCurrentInstance(this);
	}

	/**
	 * @param currentSession the currentSession to set
	 */
	public void setCurrentSession(MCPSession currentSession) {
		this.currentSession = currentSession;
	}

	@Override
	public Implementation getServerInfo() {
		// TODO Auto-generated method stub
		return serverConfig.getServerInfo();
	}

	@Override
	public ServerCapabilities getServerCapabilities() {
		// TODO Auto-generated method stub
		return serverConfig.getCapabilities();
	}

	@Override
	public ResourceManager getResourceManager() {
		// TODO Auto-generated method stub
//		return resourceManager;
		return mcpServer;
	}

	@Override
	public PromptManager getPromptManager() {
		// TODO Auto-generated method stub
//		return promptManager;
		return mcpServer;
	}

	@Override
	public ToolManager getToolManager() {
		// TODO Auto-generated method stub
//		return toolManager;
		return mcpServer;
	}

	@Override
	public RootsProvider getRootsProvider() {
		// TODO Auto-generated method stub
		return new DefaultRootsProvider((Server)ensureSession());
	}

	@Override
	public MCPLogger getCurrentLogger() {
		// TODO Auto-generated method stub
		return ensureSession().getLogger();
	}

	@Override
	public CancellationContext getCancellationContext() {
		// TODO Auto-generated method stub
		if (currentRequest == null) return null;
		return new DefaultCancellationContext(currentRequest.getRequestId(), ensureSession());
	}

	@Override
	public ElicitationContext getElicitationContext() {
		// TODO Auto-generated method stub
		ensureSession();
		return new DefaultElicitationContext(((Server)ensureSession()).getClientCapabilities(), ensureSession());
	}

	@Override
	public ProgressContext getProgressContext() {
		// TODO Auto-generated method stub
		if (currentRequest == null || currentRequest.getMeta() == null || currentRequest.getMeta().getProgressToken() == null) return null;
		return new DefaultProgressContext(currentRequest.getMeta().getProgressToken(), ensureSession());
	}

//	@Override
//	public RequestContext getCurrentRequest() {
//		// TODO Auto-generated method stub
//		return currentRequest;
//	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		serverConfig = null;
		mcpServer = null;
		currentRequest = null;
		currentSession = null;
		setCurrentInstance(null);
	}
	
	private MCPSession ensureSession() {
		if (currentSession == null) throw new IllegalStateException("No active MCP session exists.");
		return currentSession;
	}

	/**
	 * @param requestId the request ID
	 * @param meta the {@link RequestMeta}.
	 */
	public void setCurrentRequest(final RequestId requestId, final RequestMeta meta) {
		if (requestId == null) {
			if (currentRequest != null) ensureSession().remove(currentRequest.getRequestId());
			currentRequest = null;
		}
		else {
			ensureSession().create(requestId, meta);
			currentRequest = ensureSession().get(requestId);
		}
	}

	@Override
	public void cancelRequest(RequestId requestId, String reason) {
		// TODO Auto-generated method stub
		if (requestId != null) {
			if (currentRequest != null && currentRequest.getRequestId().equals(requestId)) {
				currentRequest.cancel(reason);
			} else {
				RequestContext request = ensureSession().get(requestId);
				request.cancel(reason);
			}
		}
	}
}
