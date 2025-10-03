/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Buhake Sindi
 * @since 11 September 2025
 */
public interface StreamableHttpMCPServerProcessor extends HttpMCPServerProcessor {

	public void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws IOException;
}
