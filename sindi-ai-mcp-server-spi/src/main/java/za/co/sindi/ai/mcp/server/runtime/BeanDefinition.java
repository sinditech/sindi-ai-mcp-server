/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import java.io.Serializable;
import java.util.List;

/**
 * @author Buhake Sindi
 * @since 09 April 2025
 */
public class BeanDefinition implements Serializable {

	private Class<?> beanClass;
	private Object instance;
	private List<ResourceDefinition> resources;
	private List<ResourceTemplatesDefinition> resourceTemplates;
	private List<ToolDefinition> tools;
	private List<PromptDefinition> prompts;
	
	/**
	 * @param beanClass
	 * @param instance
	 * @param resources
	 * @param resourceTemplates
	 * @param tools
	 * @param prompts
	 */
	public BeanDefinition(Class<?> beanClass, Object instance, List<ResourceDefinition> resources, List<ResourceTemplatesDefinition> resourceTemplates, List<ToolDefinition> tools,	List<PromptDefinition> prompts) {
		super();
		this.beanClass = beanClass;
		this.instance = instance;
		this.resources = resources;
		this.resourceTemplates = resourceTemplates;
		this.tools = tools;
		this.prompts = prompts;
	}
	
	/**
	 * @return the beanClass
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * @return the instance
	 */
	public Object getInstance() {
		return instance;
	}

	/**
	 * @return the resources
	 */
	public List<ResourceDefinition> getResources() {
		return resources;
	}

	/**
	 * @return the resourceTemplates
	 */
	public List<ResourceTemplatesDefinition> getResourceTemplates() {
		return resourceTemplates;
	}

	/**
	 * @return the tools
	 */
	public List<ToolDefinition> getTools() {
		return tools;
	}

	/**
	 * @return the prompts
	 */
	public List<PromptDefinition> getPrompts() {
		return prompts;
	}
}
