/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.SessionFactory;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class SessionFactoryProducer extends CDIBean<SessionFactory> {

	public SessionFactoryProducer(final SessionFactory sessionFactory) {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(sessionFactory.getClass().getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(SessionFactory.class)
			 .types(SessionFactory.class)
			 .produce(e -> sessionFactory);
	}	
}
