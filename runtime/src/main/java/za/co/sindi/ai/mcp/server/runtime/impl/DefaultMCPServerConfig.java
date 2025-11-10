/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.LoggingLevel;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.schema.ServerCapabilities.Builder;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 05 May 2025
 */
public class DefaultMCPServerConfig implements MCPServerConfig {
	
	private final String name;
	private final String version;
	private final String instructions;
	private final boolean enableLogging;
	private final boolean enableCompletions;
	private final boolean enablePrompts;
	private final boolean notifyPromptListChanged;
	private final boolean enableResources;
	private final boolean notifyResourceListChanged;
	private final boolean enableTools;
	private final boolean notifyToolListChanged;
	
	private final LoggingLevel defaultLoggingLevel;

	/**
	 * @param name
	 * @param version
	 * @param instructions
	 */
	public DefaultMCPServerConfig(String name, String version, String instructions) {
		this(name, version, instructions, false, false, false, false, false, false, false, false, LoggingLevel.INFO);
	}

	/**
	 * @param name
	 * @param version
	 * @param instructions
	 * @param enableLogging
	 * @param enableCompletions
	 * @param enablePrompts
	 * @param notifyPromptListChanged
	 * @param enableResources
	 * @param notifyResourceListChanged
	 * @param enableTools
	 * @param notifyToolListChanged
	 * @param defaultLoggingLevel
	 */
	public DefaultMCPServerConfig(String name, String version, String instructions, boolean enableLogging,
			boolean enableCompletions, boolean enablePrompts, boolean notifyPromptListChanged, boolean enableResources,
			boolean notifyResourceListChanged, boolean enableTools, boolean notifyToolListChanged, LoggingLevel defaultLoggingLevel) {
		super();
		this.name = name;
		this.version = version;
		this.instructions = instructions;
		this.enableLogging = enableLogging;
		this.enableCompletions = enableCompletions;
		this.enablePrompts = enablePrompts;
		this.notifyPromptListChanged = notifyPromptListChanged;
		this.enableResources = enableResources;
		this.notifyResourceListChanged = notifyResourceListChanged;
		this.enableTools = enableTools;
		this.notifyToolListChanged = notifyToolListChanged;
		this.defaultLoggingLevel = defaultLoggingLevel;
	}

	@Override
	public Implementation getServerInfo() {
		// TODO Auto-generated method stub
		return new Implementation(name, version);
	}

	@Override
	public String getInstructions() {
		// TODO Auto-generated method stub
		return instructions;
	}

	@Override
	public ServerCapabilities getCapabilities() {
		// TODO Auto-generated method stub
		Builder builder = new ServerCapabilities.Builder();
		if (enableCompletions)
			builder = builder.completions();
		
		if (enableLogging)
			builder = builder.logging();
		
		if (enablePrompts)
			builder = builder.prompts(notifyPromptListChanged);
			
		if (enableResources)
			builder = builder.resources(notifyResourceListChanged);
		
		if (enableTools) 
			builder = builder.tools(notifyToolListChanged);
		
		return builder.build();
	}

	@Override
	public MCPServerConfig enableAll() {
		// TODO Auto-generated method stub
		return new DefaultMCPServerConfig(name, version, instructions, true, true, true, true, true, true, true, true, defaultLoggingLevel);
	}
}
