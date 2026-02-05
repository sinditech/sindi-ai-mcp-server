/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.schema.RequestId;

/**
 * @author Buhake Sindi
 * @since 28 January 2026
 */
public interface RequestManager {
	
	public void addRequest(final RequestContext request);

	public RequestContext getRequest(final RequestId requestId);
	
	public boolean exists(final RequestId requestId);
	
	public void removeRequest(final RequestId requestId);
}
