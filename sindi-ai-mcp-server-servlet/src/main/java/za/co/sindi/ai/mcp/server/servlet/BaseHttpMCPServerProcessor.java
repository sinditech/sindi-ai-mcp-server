/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Buhake Sindi
 * @since 22 September 2025
 */
public abstract class BaseHttpMCPServerProcessor implements HttpMCPServerProcessor {
	
	protected static final String UTF_8 = "UTF-8";

	protected static void writeResponse(final HttpServletResponse response, final int statusCode, final String message, final String contentType) throws IOException {
		response.setContentType(contentType);
		response.setCharacterEncoding(UTF_8);
		response.setStatus(statusCode);
		PrintWriter writer = response.getWriter();
		writer.write(message);
		writer.flush();
	}
}
