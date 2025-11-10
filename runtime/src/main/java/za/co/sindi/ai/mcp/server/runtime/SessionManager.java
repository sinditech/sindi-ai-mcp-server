/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.server.MCPSession;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public interface SessionManager extends Iterable<MCPSession> {

	public void addSession(final String sessionId, final MCPSession session);
	public MCPSession getSession(final String sessionId);
	public MCPSession[] getSessions();
	public boolean sessionExists(final String sessionId);
	public void removeSession(final String sessionId);
	public int totalSessions();
	
}
