/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.time.Instant;

import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.schema.RequestMeta;

/**
 * @author Buhake Sindi
 * @since 28 January 2026
 */
public interface RequestContext {
	
	public Instant getCancellationTime();
	
	public String getCancellationReason();
	
	public Instant getCreationTime();

	public RequestId getRequestId();
	
	public RequestMeta getMeta();
	
	public String getSessionId();
	
	public boolean isCancelled();
	
	default void cancel() {
		cancel(null);
	}
	
	public void cancel(final String reason);
}
