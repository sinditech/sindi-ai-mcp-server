/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import za.co.sindi.ai.mcp.server.exception.CancellationException;

/**
 * @author Buhake Sindi
 * @since 23 January 2026
 */
public interface CancellationContext {

	public boolean isRequestCancelled();
	
	public String getRequestCancellationReason();
	
	default void skipProcessingIfCancelled() {
		if (isRequestCancelled()) {
			throw new CancellationException("Operation has been cancelled.");
		}
	}
}
