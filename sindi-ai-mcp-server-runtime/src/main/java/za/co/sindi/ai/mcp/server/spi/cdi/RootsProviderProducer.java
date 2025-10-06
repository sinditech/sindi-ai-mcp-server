/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.RequestScoped;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class RootsProviderProducer extends CDIBean<RootsProvider> {

	public RootsProviderProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(RootsProvider.class.getSimpleName()))
			 .scope(RequestScoped.class)
			 .beanClass(RootsProvider.class)
			 .types(RootsProvider.class)
			 .produce(e -> MCPContext.getCurrentInstance().getRootsProvider());
	}
}
