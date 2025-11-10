/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.ReadResourceRequest;
import za.co.sindi.ai.mcp.schema.ReadResourceResult;
import za.co.sindi.ai.mcp.schema.ResourceContents;
import za.co.sindi.ai.mcp.server.runtime.AbstractResultHandler;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.exception.FeatureExecutionException;
import za.co.sindi.commons.utils.Arrays;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultResourceResultHandler extends AbstractResultHandler<ReadResourceRequest, ReadResourceResult> {

	/**
	 * @param executor
	 */
	public DefaultResourceResultHandler(FeatureExecutor<ReadResourceRequest> executor) {
		super(executor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ReadResourceResult generateResult(Object value, Throwable throwable) {
		// TODO Auto-generated method stub
		if (throwable != null) {
			if (throwable instanceof FeatureExecutionException e) throw e;
			else throw new FeatureExecutionException(throwable);
		}
		
		if (!Arrays.isOfType(value, ResourceContents.class)) throw new IllegalStateException("Result is not of type " + ResourceContents.class.getName());
		ReadResourceResult result = new ReadResourceResult();
		result.setContents((ResourceContents[])value);
		return result;
	}
}
