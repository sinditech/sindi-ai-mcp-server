/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class DefaultSessionManager implements SessionManager {
	
	private final ConcurrentMap<String, Server> sessions = new ConcurrentHashMap<>();

	@Override
	public void addSession(String sessionId, Server server) {
		// TODO Auto-generated method stub
		if (sessionExists(sessionId)) throw new IllegalStateException("Session ID: " + sessionId + " already exists.");
		if (server == null) removeSession(sessionId);
		sessions.put(sessionId, server);
	}

	@Override
	public Server getSession(String sessionId) {
		// TODO Auto-generated method stub
		return sessions.get(sessionId);
	}

	@Override
	public Server[] getSessions() {
		// TODO Auto-generated method stub
		return sessions.values().toArray(new Server[totalSessions()]);
	}

	@Override
	public Iterator<Server> iterator() {
		// TODO Auto-generated method stub
		return sessions.values().iterator();
	}

	@Override
	public boolean sessionExists(String sessionId) {
		// TODO Auto-generated method stub
		return sessions.containsKey(sessionId);
	}

	@Override
	public void removeSession(String sessionId) {
		// TODO Auto-generated method stub
		sessions.remove(sessionId);
	}

	@Override
	public int totalSessions() {
		// TODO Auto-generated method stub
		return sessions.size();
	}
}
