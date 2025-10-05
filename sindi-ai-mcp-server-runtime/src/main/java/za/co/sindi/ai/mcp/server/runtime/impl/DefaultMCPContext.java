/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestParameters;
import za.co.sindi.ai.mcp.schema.ElicitResult;
import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.server.MCPSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.MCPContext;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 23 April 2025
 */
public class DefaultMCPContext extends MCPContext {

	private MCPServerConfig serverConfig;
//	private PromptManager promptManager;
//	private ResourceManager resourceManager;
//	private ToolManager toolManager;
	private MCPServer mcpServer;
	private MCPSession currentSession;
	
	/**
	 * 
	 */
	private DefaultMCPContext() {
		super();
		// TODO Auto-generated constructor stub
		setCurrentInstance(this);
	}

	/**
	 * @param serverConfig
	 * @param mcpServer
	 * @param currentSession
	 */
	public DefaultMCPContext(MCPServerConfig serverConfig, MCPServer mcpServer, MCPSession currentSession) {
		this();
		this.serverConfig = serverConfig;
		this.mcpServer = mcpServer;
		this.currentSession = currentSession;
	}

//	/**
//	 * @param serverConfig
//	 * @param promptManager
//	 * @param resourceManager
//	 * @param toolManager
//	 * @param currentServerSession
//	 */
//	public DefaultMCPContext(MCPServerConfig serverConfig, PromptManager promptManager, ResourceManager resourceManager,
//			ToolManager toolManager, MCPServerSession currentServerSession) {
//		this();
//		this.serverConfig = serverConfig;
//		this.promptManager = promptManager;
//		this.resourceManager = resourceManager;
//		this.toolManager = toolManager;
//		this.currentServerSession = currentServerSession;
//	}

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
		return new DefaultRootsProvider((Server)currentSession);
	}

	@Override
	public MCPLogger getCurrentLogger() {
		// TODO Auto-generated method stub
		return currentSession.getLogger();
	}

	@Override
	public CompletableFuture<ElicitResult> elicitInput(ElicitRequestParameters requestParameters) {
		// TODO Auto-generated method stub
		return ((Server)currentSession).elicitInput(requestParameters);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		serverConfig = null;
//		promptManager = null;
//		resourceManager = null;
//		toolManager = null;
		mcpServer = null;
		currentSession = null;
		setCurrentInstance(null);
	}
}
