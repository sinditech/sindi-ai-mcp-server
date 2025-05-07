/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.util.List;

import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public interface BeanDefinitionRegistry {
	
	public List<BeanDefinition> getBeans();
	
	public static BeanDefinitionRegistryBuilder builder() {
		// TODO Auto-generated method stub
		return new BeanDefinitionRegistryBuilder();
	}
	
	public static interface Builder<BDR extends BeanDefinitionRegistry, B extends Builder<BDR, B>> {
		
		default B fromClass(final Class<?> clazz) {
			return fromClasses(clazz);
		}
		
		public B fromClasses(final Class<?> ...classes);
		
		default B fromInstance(final Object instance) {
			return fromInstances(instance);
		}
		
		public B fromInstances(final Object ...instances);
		
		public BDR build();
	}
}
