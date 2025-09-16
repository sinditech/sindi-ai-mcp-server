/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.SessionFactory;
import za.co.sindi.ai.mcp.server.impl.MCPServerFeatureManager;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultSessionManager;
import za.co.sindi.ai.mcp.server.spi.MCPServer;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.ai.mcp.server.spi.Prompt;
import za.co.sindi.ai.mcp.server.spi.Resource;
import za.co.sindi.ai.mcp.server.spi.ResourceTemplate;
import za.co.sindi.ai.mcp.server.spi.Tool;

/**
 * @author Buhake Sindi
 * @since 02 September 2025
 */
public class MCPServerExtension implements Extension {
	
	private static final Logger LOGGER = Logger.getLogger(MCPServerExtension.class.getName());
	
	private final Set<Class<?>> MCP_FEATURE_CLASSES = new LinkedHashSet<>();
	private Class<?> mcpApplicationClass;
	
	void registerMcpTypes(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
		
    }
	
	 void enableMcpConfig(@Observes @WithAnnotations({ MCPServer.class }) final ProcessAnnotatedType<?> processAnnotatedType) {
		 mcpApplicationClass = processAnnotatedType.getAnnotatedType().getJavaClass();
	 }
	 
	 void discoverMcpFeatures(@Observes @WithAnnotations({ Prompt.class, Resource.class, ResourceTemplate.class, Tool.class }) final ProcessAnnotatedType<?> processAnnotatedType) {
		 MCP_FEATURE_CLASSES.add(processAnnotatedType.getAnnotatedType().getJavaClass());
	 }
	 
	 void addAllMcpBeans(@Observes final AfterBeanDiscovery afterBeanDiscovery) {
		if (mcpApplicationClass == null && !MCP_FEATURE_CLASSES.isEmpty()) {
			afterBeanDiscovery.addDefinitionError(new RuntimeException("An MCP application is required to register MCP features."));
			return ;
		}
		 
		MCPServer mcpServerAnnotation = mcpApplicationClass.getAnnotation(MCPServer.class);
		final MCPServerConfig mcpServerConfig = new DefaultMCPServerConfig(mcpServerAnnotation.name(), mcpServerAnnotation.version(), mcpServerAnnotation.instructions(), mcpServerAnnotation.enableLogging(), mcpServerAnnotation.enableCompletions(), mcpServerAnnotation.enablePrompts(), mcpServerAnnotation.notifyPromptListChanged(), mcpServerAnnotation.enableResources(), mcpServerAnnotation.notifyResourceListChanged(), mcpServerAnnotation.enableTools(), mcpServerAnnotation.notifyToolListChanged(),  mcpServerAnnotation.defaultLoggingLevel());
		final SessionManager sessionManager = new DefaultSessionManager();
		 
		afterBeanDiscovery.addBean()
				         .id("sindi_ai_mcp#mcpserverconfig")
				         .types(MCPServerConfig.class, Object.class)
				         .beanClass(DefaultMCPServerConfig.class)
				         .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE)
				         .scope(ApplicationScoped.class)
				         .createWith(c -> mcpServerConfig);
		 
		BeanDefinitionRegistryBuilder builder = BeanDefinitionRegistry.builder();
		builder.fromClasses(MCP_FEATURE_CLASSES.toArray(new Class[MCP_FEATURE_CLASSES.size()]));
			
		final FeatureDefinitionManager featureDefinitionManager = new DefaultFeatureDefinitionManager(builder.build().getBeans(), new DefaultFeatureExecutorFactory());
		final MCPServerFeatureManager featureManager = new MCPServerFeatureManager(mcpServerConfig.getCapabilities(), sessionManager, featureDefinitionManager);
		SessionFactory serverFactory = (transport) -> {
			MCPServerSession session = new MCPServerSession(transport, mcpServerConfig.getServerInfo(), mcpServerConfig.getCapabilities(), mcpServerConfig.getInstructions());
			featureManager.setup(session);
			session.setCloseCallback(() -> {
				LOGGER.severe("Client Disconnected: " + transport.getSessionId());
			    sessionManager.removeSession(transport.getSessionId());
			});
			return session;
		};
	 }
	 
	 void cleanup(@Observes final BeforeShutdown shutdown) {
		 
	 }
}
