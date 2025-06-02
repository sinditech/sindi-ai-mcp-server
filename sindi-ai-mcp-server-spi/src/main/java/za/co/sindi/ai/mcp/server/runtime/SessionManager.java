/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.server.Server;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public interface SessionManager extends Iterable<Server> {

	public void addSession(final String sessionId, final Server server);
	public Server getSession(final String sessionId);
	public Server[] getSessions();
	public boolean sessionExists(final String sessionId);
	public void removeSession(final String sessionId);
	public int totalSessions();
	
}
