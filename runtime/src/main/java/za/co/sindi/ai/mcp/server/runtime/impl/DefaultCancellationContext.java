/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Objects;

import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.RequestContext;
import za.co.sindi.ai.mcp.server.spi.CancellationContext;

/**
 * @author Buhake Sindi
 * @since 31 January 2026
 */
public class DefaultCancellationContext implements CancellationContext {
	
	private final RequestId requestId;
	private final MCPSession currentSession;

	/**
	 * @param requestId
	 * @param currentSession
	 */
	public DefaultCancellationContext(RequestId requestId, MCPSession currentSession) {
		super();
		this.requestId = Objects.requireNonNull(requestId);
		this.currentSession = Objects.requireNonNull(currentSession);
	}

	@Override
	public boolean isRequestCancelled() {
		// TODO Auto-generated method stub
		RequestContext request = getRequest();
		return request == null ? false : request.isCancelled();
	}

	@Override
	public String getRequestCancellationReason() {
		// TODO Auto-generated method stub
		RequestContext request = getRequest();
		return request == null ? null : request.getCancellationReason();
	}

	private RequestContext getRequest() {
		return currentSession.get(requestId);
	}
}
