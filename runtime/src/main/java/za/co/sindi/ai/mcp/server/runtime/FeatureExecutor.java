/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.schema.Request;
import za.co.sindi.ai.mcp.server.runtime.exception.FeatureExecutionException;

/**
 * @author Buhake Sindi
 * @since 15 April 2025
 */
public interface FeatureExecutor<REQ extends Request> {

	public Object invoke(final REQ request) throws FeatureExecutionException;
}
