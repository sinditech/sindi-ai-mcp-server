/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 12 May 2025
 */
public class FeatureDefinitionManagerProducer extends CDIBean<FeatureDefinitionManager> {

	public FeatureDefinitionManagerProducer(final FeatureDefinitionManager featureDefinitionManager) {
		super.name(Strings.uncapitalize(featureDefinitionManager.getClass().getSimpleName()))
			 .scope(ApplicationScoped.class)
			 .beanClass(FeatureDefinitionManager.class)
			 .types(FeatureDefinitionManager.class)
			 .produce(e -> featureDefinitionManager);
	}	
}
