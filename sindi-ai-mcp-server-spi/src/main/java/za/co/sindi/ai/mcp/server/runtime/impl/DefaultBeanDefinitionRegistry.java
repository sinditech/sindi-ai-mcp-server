/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import za.co.sindi.ai.mcp.server.runtime.BeanDefinition;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.MCPFeatures;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultBeanDefinitionRegistry implements BeanDefinitionRegistry {

	private List<BeanDefinition> beans;
	
	private DefaultBeanDefinitionRegistry(BeanDefinitionRegistryBuilder builder) {
		beans = builder.instances.entrySet().stream().map(entry -> MCPFeatures.createBeanDefinition(entry.getKey(), entry.getValue())).filter(Objects::nonNull).toList(); 
	}

	@Override
	public List<BeanDefinition> getBeans() {
		// TODO Auto-generated method stub
		return beans;
	}
	
	public static class BeanDefinitionRegistryBuilder implements Builder<DefaultBeanDefinitionRegistry, BeanDefinitionRegistryBuilder> {
		
		private Map<Class<?>, Object> instances = new HashMap<>();

		@Override
		public BeanDefinitionRegistryBuilder fromClasses(Class<?>... classes) {
			// TODO Auto-generated method stub
			for (Class<?> clazz : classes) {
				int modifiers = clazz.getModifiers();
				if (clazz != null && !Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !Object.class.equals(clazz) && !Proxy.isProxyClass(clazz)) {
					try {
						Object instance = clazz.getDeclaredConstructor().newInstance();
						instances.put(clazz, instance);
						if (!Object.class.equals(clazz.getSuperclass())) {
							instances.put(clazz.getSuperclass(), instance);
						}
						
						Class<?>[] interfaces = clazz.getInterfaces();
						if (interfaces != null) {
							for (Class<?> interfaceClass : interfaces) instances.put(interfaceClass, instance);
						}
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						// TODO Auto-generated catch block
					}
				}
			}
			return this;
		}

		@Override
		public BeanDefinitionRegistryBuilder fromInstances(Object... instances) {
			// TODO Auto-generated method stub
			for (Object instance : instances) {
				if (instance != null && !(instance instanceof Object)) {
					Class<?> clazz = instance.getClass();
					if (!Proxy.isProxyClass(clazz)) {
						this.instances.put(clazz, instance);
						if (!Object.class.equals(clazz.getSuperclass())) {
							this.instances.put(clazz.getSuperclass(), instance);
						}
						
						Class<?>[] interfaces = clazz.getInterfaces();
						if (interfaces != null) {
							for (Class<?> interfaceClass : interfaces) this.instances.put(interfaceClass, instance);
						}
					}
				}
			}
			return this;
		}

		@Override
		public DefaultBeanDefinitionRegistry build() {
			// TODO Auto-generated method stub
			return new DefaultBeanDefinitionRegistry(this);
		}
	}
}
