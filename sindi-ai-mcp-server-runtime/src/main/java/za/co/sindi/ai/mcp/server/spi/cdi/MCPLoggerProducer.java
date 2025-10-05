/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.SessionScoped;
import za.co.sindi.ai.mcp.server.runtime.MCPContext;
import za.co.sindi.ai.mcp.server.spi.MCPLogger;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class MCPLoggerProducer extends CDIBean<MCPLogger> {

	public MCPLoggerProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(MCPLogger.class.getSimpleName()))
			 .scope(SessionScoped.class)
			 .beanClass(MCPLogger.class)
			 .types(MCPLogger.class)
			 .produce(e -> MCPContext.getCurrentInstance().getCurrentLogger());
	}
}
