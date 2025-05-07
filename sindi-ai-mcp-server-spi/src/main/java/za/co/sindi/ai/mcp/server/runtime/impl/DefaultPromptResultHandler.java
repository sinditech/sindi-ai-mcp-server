/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.server.runtime.AbstractResultHandler;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultPromptResultHandler extends AbstractResultHandler<GetPromptRequest, GetPromptResult> {

	/**
	 * @param executor
	 */
	public DefaultPromptResultHandler(FeatureExecutor<GetPromptRequest> executor) {
		super(executor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected GetPromptResult generateResult(Object value, Throwable throwable) {
		// TODO Auto-generated method stub
		return null;
	}
}
