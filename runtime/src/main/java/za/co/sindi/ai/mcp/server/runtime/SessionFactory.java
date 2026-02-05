/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.shared.ServerTransport;

/**
 * @author Buhake Sindi
 * @since 06 May 2025
 */
@FunctionalInterface
public interface SessionFactory {

	public MCPSession create(final ServerTransport transport);
}
