/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Objects;

import za.co.sindi.ai.mcp.schema.ProgressNotification;
import za.co.sindi.ai.mcp.schema.ProgressNotification.ProgressNotificationParameters;
import za.co.sindi.ai.mcp.schema.ProgressToken;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.spi.ProgressContext;

/**
 * @author Buhake Sindi
 * @since 31 January 2026
 */
public class DefaultProgressContext implements ProgressContext {
	
	private final ProgressToken progressToken;
	private final MCPSession currentSession;

	/**
	 * @param progressToken
	 * @param currentSession
	 */
	public DefaultProgressContext(ProgressToken progressToken, MCPSession currentSession) {
		super();
		this.progressToken = Objects.requireNonNull(progressToken);
		this.currentSession = Objects.requireNonNull(currentSession);
	}

	@Override
	public ProgressToken getProgressToken() {
		// TODO Auto-generated method stub
		return progressToken;
	}

	@Override
	public void notifyProgress(long progress, long total, String message) {
		// TODO Auto-generated method stub
		ProgressNotificationParameters parameters = new ProgressNotificationParameters(progressToken, progress);
		parameters.setTotal(total);
		parameters.setMessage(message);
		ProgressNotification notification = new ProgressNotification(parameters);
		((Server)currentSession).sendNotification(notification);
	}
}
