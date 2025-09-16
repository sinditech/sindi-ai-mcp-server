/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.Root;

/**
 * @author Buhake Sindi
 * @since 03 May 2025
 */
public interface RootsProvider {

	public CompletableFuture<Root[]> listRoots();
}
