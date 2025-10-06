/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class MCPContextProducer extends CDIBean<MCPContext> {

	public MCPContextProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(MCPContext.class.getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(MCPContext.class)
			 .types(MCPContext.class)
			 .produce(e -> MCPContext.getCurrentInstance());
	}

	@Override
	public void destroy(MCPContext instance, CreationalContext<MCPContext> creationalContext) {
		// TODO Auto-generated method stub
		instance.release();
		super.destroy(instance, creationalContext);
	}
}
