/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.ClientCapabilities;
import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestFormParameters;
import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestFormParameters.RequestedSchema;
import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestURLParameters;
import za.co.sindi.ai.mcp.schema.ElicitResult;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.spi.ElicitationContext;

/**
 * @author Buhake Sindi
 * @since 12 January 2026
 */
public class DefaultElicitationContext implements ElicitationContext {

	private final ClientCapabilities capabilities;
	private final MCPSession currentSession;
	
	/**
	 * @param capabilities
	 * @param currentSession
	 */
	public DefaultElicitationContext(ClientCapabilities capabilities, MCPSession currentSession) {
		super();
		this.capabilities = Objects.requireNonNull(capabilities, "The client capabilities is required.");
		this.currentSession = Objects.requireNonNull(currentSession, "An MCP Session is required.");
	}

	@Override
	public boolean isElicitationSupported() {
		// TODO Auto-generated method stub
		return capabilities.getElicitation() != null;
	}

	@Override
	public CompletableFuture<ElicitResult> elicitInput(String message, RequestedSchema requestedSchema) {
		// TODO Auto-generated method stub
		if (!isElicitationSupported()) return CompletableFuture.failedFuture(new IllegalStateException("Elicitation is not enabled."));
		return ((Server)currentSession).elicitInput(new ElicitRequestFormParameters(message, requestedSchema));
	}

	@Override
	public CompletableFuture<ElicitResult> elicitInput(String message, String url, String elicitationId) {
		// TODO Auto-generated method stub
		if (!isElicitationSupported() || capabilities.getElicitation().getUrl() == null) return CompletableFuture.failedFuture(new IllegalStateException("Elicitation is not enabled."));
		return ((Server)currentSession).elicitInput(new ElicitRequestURLParameters(message, elicitationId, url));
	}
}
