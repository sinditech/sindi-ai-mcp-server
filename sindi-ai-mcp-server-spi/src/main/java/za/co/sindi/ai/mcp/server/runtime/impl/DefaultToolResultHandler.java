/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.List;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.CallToolResult;
import za.co.sindi.ai.mcp.schema.Content;
import za.co.sindi.ai.mcp.schema.TextContent;
import za.co.sindi.ai.mcp.server.runtime.AbstractResultHandler;
import za.co.sindi.ai.mcp.server.runtime.FeatureExecutor;
import za.co.sindi.commons.utils.Arrays;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public class DefaultToolResultHandler extends AbstractResultHandler<CallToolRequest, CallToolResult> {

	/**
	 * @param executor
	 */
	public DefaultToolResultHandler(FeatureExecutor<CallToolRequest> executor) {
		super(executor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected CallToolResult generateResult(Object value, Throwable throwable) {
		// TODO Auto-generated method stub
		CallToolResult result = new CallToolResult();
		List<? extends Content> contents;
		if (throwable != null) {
			contents = List.of(new TextContent(throwable.getLocalizedMessage()));
			result.setError(true);
		} else {
			if (value.getClass().isArray()) {
				if (Arrays.isArrayOfSubtype(value, Content.class)) {
					contents = List.of((Content[])value);
				} else {
					Object[] values = Arrays.toTypedArray(value);
					contents = java.util.Arrays.asList(values).stream().map(v -> new TextContent(String.valueOf(v))).toList();
				}
			} else {
				if (value instanceof Content) {
					contents = List.of((Content)value);
				} else {
					contents = List.of(new TextContent(String.valueOf(value)));
				}
			}
		}
		result.setContent(contents.toArray(new Content[contents.size()]));
		return result;
	}
}
