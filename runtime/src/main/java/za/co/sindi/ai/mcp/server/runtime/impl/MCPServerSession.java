/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import za.co.sindi.ai.mcp.schema.EmptyResult;
import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.LoggingLevel;
import za.co.sindi.ai.mcp.schema.LoggingMessageNotification;
import za.co.sindi.ai.mcp.schema.LoggingMessageNotification.LoggingMessageNotificationParameters;
import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.schema.RequestMeta;
import za.co.sindi.ai.mcp.schema.Schema;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.schema.SetLevelRequest;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.RequestContext;
import za.co.sindi.ai.mcp.server.runtime.RequestManager;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;
import za.co.sindi.ai.mcp.shared.RequestHandler;
import za.co.sindi.ai.mcp.shared.ServerTransport;

/**
 * @author Buhake Sindi
 * @since 09 September 2025
 */
public class MCPServerSession extends Server implements MCPSession {

	private final AtomicReference<LoggingLevel> loggingLevel = new AtomicReference<>(LoggingLevel.DEBUG);
	private final RequestManager requestManager = new DefaultRequestManager();

	/**
	 * @param transport
	 * @param serverInfo
	 * @param serverCapabilities
	 * @param instructions
	 */
	public MCPServerSession(final ServerTransport transport, Implementation serverInfo, ServerCapabilities serverCapabilities, String instructions) {
		super(serverInfo, serverCapabilities, instructions);
		// TODO Auto-generated constructor stub
		if (getCapabilities() != null && getCapabilities().getLogging() != null) {
			addRequestHandler(SetLevelRequest.METHOD_LOGGING_SETLEVEL, setLoggingLevelRequestHandler());
			addNotificationHandler(LoggingMessageNotification.METHOD_NOTIFICATION_LOGGING_MESSAGE, notification -> {});
		}
		setTransport(transport);
	}
	
	private RequestHandler<EmptyResult> setLoggingLevelRequestHandler() {
		
		return (request, extra) -> {
			final LoggingLevel level = LoggingLevel.of(String.valueOf(request.getParams().get("level")));
			if (level != null) {
				loggingLevel.set(level);
			}
			
			return Schema.EMPTY_RESULT;
		};
	}

	@Override
	public CompletableFuture<Void> sendLoggingMessage(final LoggingMessageNotificationParameters parameters, final String sessionId) {
		// TODO Auto-generated method stub
		if (getCapabilities().getLogging() == null || isMessageIgnored(parameters.getLevel())) {
			return CompletableFuture.completedFuture(null);
		}
		
		LoggingMessageNotification notification = new LoggingMessageNotification(parameters);
//		notification.setParameters(parameters);
		return sendNotification(notification);
	}
	
	private boolean isMessageIgnored(final LoggingLevel level) {
		final LoggingLevel currentLevel = loggingLevel.get();
		return level.ordinal() < currentLevel.ordinal();
	}

	public MCPLogger getLogger() {
		if (getCapabilities().getLogging() != null) return new DefaultMCPLogger(this);
		return null;
	}
	
	/**
	 * @return the loggingLevel
	 */
	@Override
	public LoggingLevel getLoggingLevel() {
		return loggingLevel.get();
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return getTransport().getSessionId();
	}

	@Override
	public void create(RequestId requestId, RequestMeta meta) {
		// TODO Auto-generated method stub
		if (requestId != null && requestManager.exists(requestId)) return ;
		requestManager.addRequest(new DefaultRequestContext(getId(), requestId, meta));
	}

	@Override
	public RequestContext get(RequestId requestId) {
		// TODO Auto-generated method stub
		if (requestId != null && requestManager.exists(requestId)) return requestManager.getRequest(requestId);
		return null;
	}

	@Override
	public void remove(RequestId requestId) {
		// TODO Auto-generated method stub
		if (requestId != null) requestManager.removeRequest(requestId);
	}
}
