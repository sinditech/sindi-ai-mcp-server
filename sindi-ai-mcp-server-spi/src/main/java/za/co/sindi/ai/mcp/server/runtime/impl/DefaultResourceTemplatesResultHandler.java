/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesResult;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.server.runtime.AbstractResultHandler;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;
import za.co.sindi.ai.mcp.server.runtime.exception.FeatureExecutionException;
import za.co.sindi.commons.utils.Arrays;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultResourceTemplatesResultHandler extends AbstractResultHandler<ListResourceTemplatesRequest, ListResourceTemplatesResult> {

	/**
	 * @param executor
	 */
	public DefaultResourceTemplatesResultHandler(FeatureExecutor<ListResourceTemplatesRequest> executor) {
		super(executor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ListResourceTemplatesResult generateResult(Object value, Throwable throwable) {
		// TODO Auto-generated method stub
		if (throwable != null) {
			if (throwable instanceof FeatureExecutionException e) throw e;
			else throw new FeatureExecutionException(throwable);
		}
		
		if (!Arrays.isOfType(value, ResourceTemplate.class)) throw new IllegalStateException("Result is not of type " + ResourceTemplate.class.getName());
		ListResourceTemplatesResult result = new ListResourceTemplatesResult();
		result.setResourceTemplates((ResourceTemplate[])value);
		return result;
	}
}
