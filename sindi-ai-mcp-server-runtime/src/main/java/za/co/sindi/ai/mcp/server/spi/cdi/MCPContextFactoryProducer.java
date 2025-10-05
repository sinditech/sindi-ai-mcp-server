/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class MCPContextFactoryProducer extends CDIBean<MCPContextFactory> {

	public MCPContextFactoryProducer(final MCPContextFactory mcpContextFactory) {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(mcpContextFactory.getClass().getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(MCPContextFactory.class)
			 .types(MCPContextFactory.class)
			 .produce(e -> mcpContextFactory);
	}
}
