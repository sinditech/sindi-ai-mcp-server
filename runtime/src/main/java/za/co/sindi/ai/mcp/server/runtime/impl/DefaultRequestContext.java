/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.time.Instant;
import java.util.Objects;

import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.schema.RequestMeta;
import za.co.sindi.ai.mcp.server.runtime.RequestContext;

/**
 * @author Buhake Sindi
 * @since 28 January 2026
 */
public class DefaultRequestContext implements RequestContext {
	
	private final String sessionId;
	private final RequestId requestId;
	private final RequestMeta meta;
	private boolean cancelled;
	private Instant creationTime;
	private Instant cancellationTime;
	private String cancellationReason;

	/**
	 * @param sessionId
	 * @param requestId
	 * @param meta
	 */
	public DefaultRequestContext(String sessionId, RequestId requestId, RequestMeta meta) {
		super();
		this.sessionId = Objects.requireNonNull(sessionId);
		this.requestId = Objects.requireNonNull(requestId);
		this.meta = meta;
		this.cancelled = false;
		this.creationTime = Instant.now();
	}

	@Override
	public Instant getCancellationTime() {
		// TODO Auto-generated method stub
		return cancellationTime;
	}

	@Override
	public String getCancellationReason() {
		// TODO Auto-generated method stub
		return cancellationReason;
	}

	@Override
	public Instant getCreationTime() {
		// TODO Auto-generated method stub
		return creationTime;
	}

	@Override
	public RequestId getRequestId() {
		// TODO Auto-generated method stub
		return requestId;
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId;
	}

	@Override
	public RequestMeta getMeta() {
		// TODO Auto-generated method stub
		return meta;
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return cancelled;
	}

	@Override
	public void cancel(String reason) {
		// TODO Auto-generated method stub
		if (!cancelled) {
			cancelled = true;
			cancellationTime = Instant.now();
			cancellationReason = reason;
		}
	}
}
