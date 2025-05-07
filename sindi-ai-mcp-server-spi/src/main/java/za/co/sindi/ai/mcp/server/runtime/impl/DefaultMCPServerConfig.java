/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.LoggingLevel;
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
	private final boolean enableSampling;
	
	private final LoggingLevel defaultLoggingLevel;

	/**
	 * @param name
	 * @param version
	 * @param instructions
	 */
	public DefaultMCPServerConfig(String name, String version, String instructions) {
		this(name, version, instructions, false, false, false, false, false, false, false, false, false, LoggingLevel.INFO);
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
			boolean notifyResourceListChanged, boolean enableTools, boolean notifyToolListChanged, boolean enableSampling,
			LoggingLevel defaultLoggingLevel) {
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
		this.enableSampling = enableSampling;
		this.defaultLoggingLevel = defaultLoggingLevel;
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public String getServerVersion() {
		// TODO Auto-generated method stub
		return version;
	}

	@Override
	public String getInstructions() {
		// TODO Auto-generated method stub
		return instructions;
	}

	@Override
	public boolean shouldEnableLogging() {
		// TODO Auto-generated method stub
		return enableLogging;
	}

	@Override
	public LoggingLevel getDefaultLoggingLevel() {
		// TODO Auto-generated method stub
		return defaultLoggingLevel;
	}

	@Override
	public boolean shouldEnableCompletions() {
		// TODO Auto-generated method stub
		return enableCompletions;
	}

	@Override
	public boolean shouldEnablePrompts() {
		// TODO Auto-generated method stub
		return enablePrompts;
	}

	@Override
	public boolean notifyPromptListChanged() {
		// TODO Auto-generated method stub
		return notifyPromptListChanged;
	}

	@Override
	public boolean shouldEnableResources() {
		// TODO Auto-generated method stub
		return enableResources;
	}

	@Override
	public boolean notifyResourceListChanged() {
		// TODO Auto-generated method stub
		return notifyResourceListChanged;
	}

	@Override
	public boolean shouldEnableTools() {
		// TODO Auto-generated method stub
		return enableTools;
	}

	@Override
	public boolean notifyToolListChanged() {
		// TODO Auto-generated method stub
		return notifyToolListChanged;
	}

	@Override
	public boolean shouldEnableSampling() {
		// TODO Auto-generated method stub
		return enableSampling;
	}

	@Override
	public MCPServerConfig enableAll() {
		// TODO Auto-generated method stub
		return new DefaultMCPServerConfig(name, version, instructions, true, true, true, true, true, true, true, true, true, defaultLoggingLevel);
	}
}
