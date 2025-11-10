/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import za.co.sindi.ai.mcp.server.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.BeanInstance;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.MCPFeatures;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.runtime.SessionFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.ContextualBeanInstance;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanInstance;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServer;
import za.co.sindi.ai.mcp.server.runtime.impl.MCPServerSession;
import za.co.sindi.ai.mcp.server.runtime.impl.ServletContextResourceContext;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.resource.Resource;
import za.co.sindi.resource.scanner.ClassScanner;
import za.co.sindi.resource.scanner.ScanningException;
import za.co.sindi.resource.scanner.impl.ResourceClassScanner;
import za.co.sindi.resource.scanner.impl.ResourceContextResourceScanner;
import za.co.sindi.resource.utils.ResourceUtils;

/**
 * @author Buhake Sindi
 * @since 02 October 2025
 */
@ApplicationScoped
public class MCPInitializer {
	
	private static final Logger LOGGER = Logger.getLogger(MCPInitializer.class.getName());
	
	@Inject
	private BeanManager beanManager;
	
	@Inject
	private MCPServerConfig mcpServerConfig;
	
	@Inject
	private SessionManager sessionManager;
	
	@Produces @ApplicationScoped
	private MCPServer mcpServer;
	
	@Produces @ApplicationScoped
	private SessionFactory sessionFactory;

	public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext servletContext) {
		try {
			ResourceContextResourceScanner resourceScanner = new ResourceContextResourceScanner(new ServletContextResourceContext(servletContext));
			resourceScanner.addResourceFilter(filter -> filter.getPath().endsWith(".jar") || filter.getPath().endsWith(".class"));
			resourceScanner.addResourcePath("/");
			Collection<Resource> resources = resourceScanner.scan();
			ClassScanner classScanner = new ResourceClassScanner(ResourceUtils.buildClassLoader(resources));
			classScanner.addTypeFilter(clazz -> {
				final Set<Class<? extends Annotation>> mcpAnnotationSet = Set.of(MCPFeatures.MCP_METHOD_ANNOTATIONS);
				for (Method method: clazz.getDeclaredMethods()) {
					for (Annotation annotation: method.getAnnotations()) {
						if (mcpAnnotationSet.contains(annotation.annotationType())) {
							return true;
						}
					}
				}
				
				return false;
			});
			Collection<Class<?>> classes = new LinkedHashSet<>();
			resources.stream().forEach(resource -> classes.addAll(classScanner.scan(resource)));
			
			BeanDefinitionRegistryBuilder builder = BeanDefinitionRegistry.builder();
			classes.stream().map(clazz -> toBeanInstance(clazz)).forEach(builder::fromBeanInstance);
//			builder.fromClasses(classes.toArray(new Class[classes.size()]));
			
			FeatureDefinitionManager featureDefinitionManager = new DefaultFeatureDefinitionManager(builder.build().getBeans(), new DefaultFeatureExecutorFactory());
			mcpServer = new DefaultMCPServer(mcpServerConfig.getCapabilities(), sessionManager, featureDefinitionManager); //new DefaultMCPServer(thisServer, featureDefinitionManager);
			sessionFactory = (transport) -> {
				MCPServerSession session = new MCPServerSession(transport, mcpServerConfig.getServerInfo(), mcpServerConfig.getCapabilities(), mcpServerConfig.getInstructions());
				mcpServer.setup(session);
				session.setCloseCallback(() -> {
					LOGGER.info("Client Disconnected: " + transport.getSessionId());
				    sessionManager.removeSession(transport.getSessionId());
				});
				return session;
			};
		} catch (IOException | ScanningException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
	
	public void cleanup(@Observes @BeforeDestroyed(ApplicationScoped.class) ServletContext servletContext) {
		if (sessionManager.totalSessions() > 0) {
			Iterator<MCPSession> itr = sessionManager.iterator();
			while (itr.hasNext()) {
				MCPSession session = itr.next();
				session.closeQuietly();
				LOGGER.info("Client Disconnected: " + session.getId());
				itr.remove();
			}
		}
		
		//Clean MCPContext
		MCPContext mcpContext = MCPContext.getCurrentInstance();
		if (mcpContext != null) {
			mcpContext.release();
			mcpContext = null;
		}
    }
	
	private <T> BeanInstance toBeanInstance(final Class<T> clazz) {
		Bean<?> bean  = beanManager.resolve(beanManager.getBeans(clazz));
		if (bean != null) {
			return new ContextualBeanInstance(clazz, beanManager);
		}
			
		try {
			return new DefaultBeanInstance(clazz);
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
	}
}
