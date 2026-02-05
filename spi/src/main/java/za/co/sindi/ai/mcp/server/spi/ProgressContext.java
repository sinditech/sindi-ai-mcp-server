/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import za.co.sindi.ai.mcp.schema.ProgressToken;

/**
 * @author Buhake Sindi
 * @since 31 January 2026
 */
public interface ProgressContext {

	public ProgressToken getProgressToken();
	
	public void notifyProgress(final long progress, final long total, final String message);
}
