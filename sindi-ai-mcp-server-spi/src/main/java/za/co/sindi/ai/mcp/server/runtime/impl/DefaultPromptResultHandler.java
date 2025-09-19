/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.List;

import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.schema.PromptMessage;
import za.co.sindi.ai.mcp.server.runtime.AbstractResultHandler;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;
import za.co.sindi.commons.utils.Arrays;

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
		GetPromptResult result = new GetPromptResult();
		List<PromptMessage> messages =  List.of();
		
		if (value.getClass().isArray()) {
			if (Arrays.isArrayOfSubtype(value, PromptMessage.class)) {
				messages = List.of((PromptMessage[])value);
			} 
		} else if (value instanceof PromptMessage) {
			messages = List.of((PromptMessage)value);
		}
		
		result.setMessages(messages.toArray(new PromptMessage[messages.size()]));
		return result;
	}
}
