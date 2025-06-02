/**
 * 
 */
package za.co.sindi.ai.mcp.server.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultSessionManager;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class SessionManagerProducer extends CDIBean<SessionManager> {

	public SessionManagerProducer(final BeanManager beanManager) {
		super.name(Strings.uncapitalize(SessionManager.class.getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(SessionManager.class)
			 .types(SessionManager.class)
			 .produce(e -> new DefaultSessionManager());
	}
}
