/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.ai.mcp.server.spi.ProgressContext;
import za.co.sindi.ai.mcp.server.spi.WithProgress;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 28 January 2026
 */
public class ProgressContextProducer extends CDIBean<ProgressContext> {

	public ProgressContextProducer() {
		super.name("sindi_ai_mcp#" + Strings.uncapitalize(ProgressContext.class.getSimpleName()))
			 .beanClass(ProgressContext.class)
			 .types(ProgressContext.class)
			 .qualifiers(WithProgress.Literal.INSTANCE, Default.Literal.INSTANCE, Any.Literal.INSTANCE)
			 .produce(e -> MCPContext.getCurrentInstance().getProgressContext());
	}
}
