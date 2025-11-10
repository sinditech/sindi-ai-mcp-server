/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class ToolManagerProducer extends CDIBean<ToolManager> {

	public ToolManagerProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(ToolManager.class.getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(ToolManager.class)
			 .types(ToolManager.class)
			 .produce(e -> MCPContext.getCurrentInstance().getToolManager());
	}
}
