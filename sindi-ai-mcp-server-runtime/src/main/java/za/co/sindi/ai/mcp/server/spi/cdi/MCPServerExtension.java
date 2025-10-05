/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import java.util.UUID;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 02 September 2025
 */
public class MCPServerExtension implements Extension {

	private static final Logger LOGGER = Logger.getLogger(MCPServerExtension.class.getName());

	void addAllMcpBeans(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		final MCPServerConfig mcpServerConfig;
		Bean<?> bean  = beanManager.resolve(beanManager.getBeans(MCPServerConfig.class));
		if (bean == null) {
			mcpServerConfig = new DefaultMCPServerConfig("Sindi AI MCP", "1.0.0", null).enableAll();
			afterBeanDiscovery.addBean()
							.id("sindi_ai_mcp#mcpserverconfig")
							.types(MCPServerConfig.class, Object.class)
							.beanClass(DefaultMCPServerConfig.class)
							.qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
							.scope(ApplicationScoped.class)
							.createWith(c -> mcpServerConfig);
		}

		afterBeanDiscovery.addBean(new EventStoreProducer());
		afterBeanDiscovery.addBean(new MCPContextFactoryProducer(new DefaultMCPContextFactory()));
		afterBeanDiscovery.addBean(new MCPContextProducer());
		afterBeanDiscovery.addBean(new MCPLoggerProducer());
		afterBeanDiscovery.addBean(new PromptManagerProducer());
		afterBeanDiscovery.addBean(new ResourceManagerProducer());
		afterBeanDiscovery.addBean(new RootsProviderProducer());
//		afterBeanDiscovery.addBean(new SessionFactoryProducer(serverFactory));
		afterBeanDiscovery.addBean(new SessionIdGeneratorProducer(() -> UUID.randomUUID().toString()));
		afterBeanDiscovery.addBean(new SessionManagerProducer());
		afterBeanDiscovery.addBean(new ToolManagerProducer());
	}
}
