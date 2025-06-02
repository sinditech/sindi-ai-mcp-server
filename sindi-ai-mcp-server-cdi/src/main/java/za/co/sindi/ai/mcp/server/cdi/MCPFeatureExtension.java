/**
 * 
 */
package za.co.sindi.ai.mcp.server.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import za.co.sindi.ai.mcp.server.runtime.MCPFeatures;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class MCPFeatureExtension implements Extension {

	public static final Set<Class<? extends Annotation>> MCP_FEATURE_ANNOTATIONS = Stream.of(MCPFeatures.MCP_METHOD_ANNOTATIONS).collect(Collectors.toSet());
	
	// Keep track of classes to register
    private final Set<Class<?>> mcpFeatureClasses = new HashSet<>();
    
    // Phase 1: scan each type for our annotations
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat) {
        Class<T> clazz = pat.getAnnotatedType().getJavaClass();
        Arrays.stream(clazz.getDeclaredMethods()).filter(method -> containsMcpFeatureAnnotations(method)).forEach(method -> mcpFeatureClasses.add(clazz));
    }
    
    // Phase 2: register each collected class as a CDI bean
    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        for (Class<?> beanClass : mcpFeatureClasses) {
//            abd.addBean(new BeanBuilder<>(beanClass, bm).build());
        }
    }
	
	private boolean containsMcpFeatureAnnotations(final Method method) {
		for (Class<? extends Annotation> annotationClass : MCP_FEATURE_ANNOTATIONS) {
			if (method.isAnnotationPresent(annotationClass)) return true;
		}
		
		return false;
	}
}
