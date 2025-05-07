/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.shared.RequestHandler;

/**
 * @author Buhake Sindi
 * @since 01 May 2025
 */
public interface PromptManager {

	public void addPrompt(final Prompt prompt, final RequestHandler<GetPromptResult> promptProvider);
	public void removePrompt(final String promptName);
}
