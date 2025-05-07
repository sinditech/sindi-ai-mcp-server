/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import za.co.sindi.ai.mcp.schema.LoggingLevel;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public interface MCPServerConfig {

	public String getServerName();
	
	public String getServerVersion();
	
	public String getInstructions();
	
	default boolean shouldEnableLogging() {
		return false;
	}
	
	default LoggingLevel getDefaultLoggingLevel() {
		return LoggingLevel.INFO;
	}
	
	default boolean shouldEnableCompletions() {
		return false;
	}
	
	default boolean shouldEnablePrompts() {
		return false;
	}
	
	default boolean notifyPromptListChanged() {
		return false;
	}
	
	default boolean shouldEnableResources() {
		return false;
	}
	
	default boolean notifyResourceListChanged() {
		return false;
	}
	
	default boolean shouldEnableTools() {
		return false;
	}
	
	default boolean notifyToolListChanged() {
		return false;
	}
	
	default boolean shouldEnableSampling() {
		return false;
	}
	
	public MCPServerConfig enableAll();
}
