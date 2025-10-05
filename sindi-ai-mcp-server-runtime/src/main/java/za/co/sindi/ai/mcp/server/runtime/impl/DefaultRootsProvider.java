/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import za.co.sindi.ai.mcp.schema.Root;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.Server;

/**
 * @author Buhake Sindi
 * @since 15 September 2025
 */
public class DefaultRootsProvider implements RootsProvider {

	private final Server session;
	
	/**
	 * @param session
	 */
	public DefaultRootsProvider(Server session) {
		super();
		this.session = Objects.requireNonNull(session, "An MCP server session is required");
	}

	@Override
	public CompletableFuture<Root[]> listRoots() {
		// TODO Auto-generated method stub
		return session.listRoots();
	}
}
