/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.MCPContext;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 23 April 2025
 */
public class DefaultMCPContext extends MCPContext {

	private MCPServerConfig serverConfig;
	private PromptManager promptManager;
	private ResourceManager resourceManager;
	private ToolManager toolManager;
	private MCPServerSession currentServerSession;
	
	/**
	 * 
	 */
	private DefaultMCPContext() {
		super();
		// TODO Auto-generated constructor stub
		MCPContext.setCurrentInstance(this);
	}

	/**
	 * @param serverConfig
	 * @param promptManager
	 * @param resourceManager
	 * @param toolManager
	 * @param currentServerSession
	 */
	public DefaultMCPContext(MCPServerConfig serverConfig, PromptManager promptManager, ResourceManager resourceManager,
			ToolManager toolManager, MCPServerSession currentServerSession) {
		this();
		this.serverConfig = serverConfig;
		this.promptManager = promptManager;
		this.resourceManager = resourceManager;
		this.toolManager = toolManager;
		this.currentServerSession = currentServerSession;
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
		return resourceManager;
	}

	@Override
	public PromptManager getPromptManager() {
		// TODO Auto-generated method stub
		return promptManager;
	}

	@Override
	public ToolManager getToolManager() {
		// TODO Auto-generated method stub
		return toolManager;
	}

	@Override
	public RootsProvider getRootsProvider() {
		// TODO Auto-generated method stub
		return new CurrentSessionRootsProvider(currentServerSession);
	}

	@Override
	public MCPLogger getCurrentLogger() {
		// TODO Auto-generated method stub
		return currentServerSession.getLogger();
	}
}
