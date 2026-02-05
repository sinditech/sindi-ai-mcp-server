/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.server.runtime.RequestContext;
import za.co.sindi.ai.mcp.server.runtime.RequestManager;

/**
 * @author Buhake Sindi
 * @since 28 January 2026
 */
public class DefaultRequestManager implements RequestManager {
	
	private final Map<RequestId, RequestContext> requests = new ConcurrentHashMap<>();

	@Override
	public void addRequest(RequestContext request) {
		// TODO Auto-generated method stub
		if (request != null) {
			requests.put(request.getRequestId(), request);
		}
	}

	@Override
	public RequestContext getRequest(RequestId requestId) {
		// TODO Auto-generated method stub
		return requestId == null ? null : requests.get(requestId);
	}

	@Override
	public boolean exists(RequestId requestId) {
		// TODO Auto-generated method stub
		return requestId == null ? false : requests.containsKey(requestId);
	}

	@Override
	public void removeRequest(RequestId requestId) {
		// TODO Auto-generated method stub
		if (requestId != null) requests.remove(requestId);
	}
}
