/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import java.util.Iterator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultSessionManager;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class SessionManagerProducer extends CDIBean<SessionManager> {

	public SessionManagerProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(SessionManager.class.getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(SessionManager.class)
			 .types(SessionManager.class)
			 .produce(e -> new DefaultSessionManager());
	}

	@Override
	public void destroy(SessionManager sessionManager, CreationalContext<SessionManager> creationalContext) {
		// TODO Auto-generated method stub
		super.destroy(sessionManager, creationalContext);
		if (sessionManager.totalSessions() > 0) {
			Iterator<MCPSession> itr = sessionManager.iterator();
			while (itr.hasNext()) {
				MCPSession session = itr.next();
				session.closeQuietly();
				itr.remove();
			}
		}
	}
}
