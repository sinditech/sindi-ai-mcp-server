/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.commons.utils.Preconditions;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class DefaultSessionManager implements SessionManager {
	
	private final ConcurrentMap<String, MCPServerSession> sessions = new ConcurrentHashMap<>();

	@Override
	public void addSession(String sessionId, MCPServerSession session) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(sessionId), "An MCP Session ID is required.");
		if (session == null) removeSession(sessionId);
		if (sessionExists(sessionId)) throw new IllegalStateException("Session ID: " + sessionId + " already exists.");
		sessions.put(sessionId, session);
	}

	@Override
	public MCPServerSession getSession(String sessionId) {
		// TODO Auto-generated method stub
		return sessions.get(sessionId);
	}

	@Override
	public MCPServerSession[] getSessions() {
		// TODO Auto-generated method stub
		return sessions.values().toArray(new MCPServerSession[totalSessions()]);
	}

	@Override
	public Iterator<MCPServerSession> iterator() {
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
