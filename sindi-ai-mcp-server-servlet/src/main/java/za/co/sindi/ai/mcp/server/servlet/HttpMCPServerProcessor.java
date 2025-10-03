/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.Implementation;

/**
 * @author Buhake Sindi
 * @since 11 September 2025
 */
public interface HttpMCPServerProcessor {

	public Implementation getServerInfo();
	
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException;
	
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException;
	
}
