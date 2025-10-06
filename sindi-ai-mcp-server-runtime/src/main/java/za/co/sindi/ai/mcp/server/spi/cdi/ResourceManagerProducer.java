/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class ResourceManagerProducer extends CDIBean<ResourceManager> {

	public ResourceManagerProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(ResourceManager.class.getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(ResourceManager.class)
			 .types(ResourceManager.class)
			 .produce(e -> MCPContext.getCurrentInstance().getResourceManager());
	}
}
