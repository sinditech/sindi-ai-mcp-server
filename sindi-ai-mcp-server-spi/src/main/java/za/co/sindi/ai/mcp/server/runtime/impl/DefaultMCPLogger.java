/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import za.co.sindi.ai.mcp.schema.LoggingLevel;
import za.co.sindi.ai.mcp.schema.LoggingMessageNotification.LoggingMessageNotificationParameters;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;

/**
 * @author Buhake Sindi
 * @since 12 September 2025
 */
public class DefaultMCPLogger implements MCPLogger {
	
	private final Logger LOGGER;
	private final MCPServerSession serverSession;

	/**
	 * @param serverSession
	 */
	public DefaultMCPLogger(final MCPServerSession serverSession) {
		super();
		this.serverSession = Objects.requireNonNull(serverSession, "An MCP server session is required.");
		this.LOGGER = Logger.getLogger("mcp-logger-" + serverSession.getTransport().getSessionId());
	}

	@Override
	public LoggingLevel getLevel() {
		// TODO Auto-generated method stub
		return serverSession.getLoggingLevel();
	}

	@Override
	public void send(LoggingLevel level, Object data) {
		// TODO Auto-generated method stub
		if (isLevelEnabled(level)) {
			LoggingMessageNotificationParameters parameters = new LoggingMessageNotificationParameters();
			parameters.setData(data);
			parameters.setLevel(getLevel());
			parameters.setLogger(LOGGER.getName());
			serverSession.sendLoggingMessage(parameters);
		}
	}

	@Override
	public void send(LoggingLevel level, String format, Object... params) {
		// TODO Auto-generated method stub
		String message = String.format(format, params);
		send(level, message);
	}

	@Override
	public void debug(String format, Object... params) {
		// TODO Auto-generated method stub
		LOGGER.log(Level.FINE, format, params);
		send(LoggingLevel.DEBUG, format, params);
	}

	@Override
	public void info(String format, Object... params) {
		// TODO Auto-generated method stub
		LOGGER.log(Level.INFO, format, params);
		send(LoggingLevel.INFO, format, params);
	}

	@Override
	public void error(String format, Object... params) {
		// TODO Auto-generated method stub
		LOGGER.log(Level.SEVERE, format, params);
		send(LoggingLevel.ERROR, format, params);
	}

	@Override
	public void error(Throwable t, String format, Object... params) {
		// TODO Auto-generated method stub
		LOGGER.log(Level.SEVERE, String.format(format, params), t);
		send(LoggingLevel.ERROR, format, params);
	}
	
	private boolean isLevelEnabled(LoggingLevel level) {
        return getLevel().ordinal() <= level.ordinal();
    }
}
