/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.schema.Root;

/**
 * @author Buhake Sindi
 * @since 03 May 2025
 */
public interface RootsProvider {

	public Root[] listRoots(final Server server);
}
