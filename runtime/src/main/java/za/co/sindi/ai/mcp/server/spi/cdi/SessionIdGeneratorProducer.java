/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.runtime.streamable.SessionIdGenerator;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class SessionIdGeneratorProducer extends CDIBean<SessionIdGenerator> {

	public SessionIdGeneratorProducer(final SessionIdGenerator sessionIdGenerator) {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(sessionIdGenerator.getClass().getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(SessionIdGenerator.class)
			 .types(SessionIdGenerator.class)
			 .produce(e -> sessionIdGenerator);
	}	
}
