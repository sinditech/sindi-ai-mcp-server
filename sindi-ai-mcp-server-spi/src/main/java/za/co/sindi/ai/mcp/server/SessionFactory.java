/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.shared.ServerTransport;

/**
 * @author Buhake Sindi
 * @since 06 May 2025
 */
@FunctionalInterface
public interface SessionFactory {

	public MCPServerSession create(final ServerTransport transport);
}
