/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.ErrorCodes;

/**
 * @author Buhake Sindi
 * @since 25 May 2025
 */
@WebServlet(value = "/mcp", asyncSupported = true)
public class StreamableHTTPServerServlet extends HttpServlet /* implements MCPServerTransportProvider */ {
	
	private static final Logger LOGGER = Logger.getLogger(StreamableHTTPServerServlet.class.getName());
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("DELETE", "GET", "POST");
	
	@Inject
	private StreamableHttpMCPServerProcessor mcpServerProcessor;

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return getClass().getName();
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
        String requestMethod = request.getMethod();
        if (!ALLOWED_HTTP_METHODS.contains(requestMethod)) {
        	DefaultStreamableHttpMCPServerProcessor.writeResponse(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, DefaultStreamableHttpMCPServerProcessor.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Method not allowed."));
//        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP request method '" + requestMethod + "' is not supported.");
        	return ;
        }
        
        super.service(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		mcpServerProcessor.doGet(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		mcpServerProcessor.doPost(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		mcpServerProcessor.doDelete(request, response);
	}
}
