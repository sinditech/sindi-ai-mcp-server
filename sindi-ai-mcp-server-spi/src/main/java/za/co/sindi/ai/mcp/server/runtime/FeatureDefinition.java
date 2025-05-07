/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

/**
 * @author Buhake Sindi
 * @since 16 April 2025
 */
public interface FeatureDefinition<T> {

	public T toMCPFeature();
}
