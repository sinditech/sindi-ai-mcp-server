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

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@WebServlet(value = "/*", asyncSupported = true)
public class SSEServerServlet extends HttpServlet /* implements MCPServerTransportProvider */ {
	
	private static final Logger LOGGER = Logger.getLogger(SSEServerServlet.class.getName());
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("GET", "POST");
	
	@Inject
	private SSEHttpMCPServerProcessor mcpServerProcessor;
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
        String requestMethod = request.getMethod();
        if (!ALLOWED_HTTP_METHODS.contains(requestMethod)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP request method '" + requestMethod + "' is not supported.");
        	return ;
        }
        
        super.service(request, response);
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return getClass().getName();
	}

	/* (non-Javadoc)
	 * @see jakarta.servlet.http.HttpServlet#doGet(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		mcpServerProcessor.doGet(request, response);
	}

	/* (non-Javadoc)
	 * @see jakarta.servlet.http.HttpServlet#doPost(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		mcpServerProcessor.doPost(request, response);
	}
}
