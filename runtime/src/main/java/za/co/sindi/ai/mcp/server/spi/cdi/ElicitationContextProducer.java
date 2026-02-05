/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import za.co.sindi.ai.mcp.server.spi.ElicitationContext;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 January 2026
 */
public class ElicitationContextProducer extends CDIBean<ElicitationContext> {

	public ElicitationContextProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(ElicitationContext.class.getSimpleName()))
			 .beanClass(ElicitationContext.class)
			 .types(ElicitationContext.class)
			 .produce(e -> MCPContext.getCurrentInstance().getElicitationContext());
	}
}
