/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class PromptManagerProducer extends CDIBean<PromptManager> {

	public PromptManagerProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(PromptManager.class.getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(PromptManager.class)
			 .types(PromptManager.class)
			 .produce(e -> MCPContext.getCurrentInstance().getPromptManager());
	}
}
