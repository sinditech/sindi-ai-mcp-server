/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.ElicitRequest.ElicitRequestFormParameters.RequestedSchema;
import za.co.sindi.ai.mcp.schema.ElicitResult;

/**
 * @author Buhake Sindi
 * @since 12 January 2026
 */
public interface ElicitationContext {
	
	public boolean isElicitationSupported();

	public CompletableFuture<ElicitResult> elicitInput(final String message, final RequestedSchema requestedSchema);
	
	public abstract CompletableFuture<ElicitResult> elicitInput(final String message, final String url, final String elicitationId);
}
