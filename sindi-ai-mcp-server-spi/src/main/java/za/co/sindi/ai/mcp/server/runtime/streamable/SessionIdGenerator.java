/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.streamable;

/**
 * @author Buhake Sindi
 * @since 10 May 2025
 */
@FunctionalInterface
public interface SessionIdGenerator {

	public String generate();
}
