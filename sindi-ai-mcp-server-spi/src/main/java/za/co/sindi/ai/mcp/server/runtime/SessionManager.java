/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.server.MCPServerSession;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public interface SessionManager extends Iterable<MCPServerSession> {

	public void addSession(final String sessionId, final MCPServerSession session);
	public MCPServerSession getSession(final String sessionId);
	public MCPServerSession[] getSessions();
	public boolean sessionExists(final String sessionId);
	public void removeSession(final String sessionId);
	public int totalSessions();
	
}
