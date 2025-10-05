package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.server.MCPSession;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.runtime.SessionFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.commons.utils.IOUtils;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@WebServlet(value = "/*", asyncSupported = true)
public class SSEServerServlet extends HttpServlet /* implements MCPServerTransportProvider */ {
	
	private static final Logger LOGGER = Logger.getLogger(SSEServerServlet.class.getName());
	
	private static final String UTF_8 = "UTF-8";
	
	/** Default endpoint path for SSE connections */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_MESSAGE_ENDPOINT = "/message";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_SESSIONID_PARAMETER_NAME = "sessionId";
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("GET", "POST");
	
	/** Map of active client sessions, keyed by session ID */
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private MCPContextFactory mcpContextFactory;
	
	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private MCPServerConfig mcpServerConfig;
	
	@Inject
	private MCPServer mcpServer;
	
	@Resource
	private ManagedExecutorService managedExecutorService;

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
		String pathInfo = request.getPathInfo();
		if (!DEFAULT_SSE_ENDPOINT.equals(pathInfo)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String sessionId = request.getParameter(DEFAULT_SESSIONID_PARAMETER_NAME);
		if (sessionId != null && sessionManager.sessionExists(sessionId)) {
			LOGGER.warning("Client Reconnecting? This shouldn't happen; when client has a sessionId (found value " + sessionId + "), GET " + DEFAULT_SSE_ENDPOINT + " should not be called again.");
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "This GET method cannot be called when a Session ID is present.", "text/plain");
			return ;
		}
				
		response.setContentType("text/event-stream");
		response.setCharacterEncoding(UTF_8);
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		response.setHeader("Access-Control-Allow-Origin", "*");

		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);

		SSEHttpServletTransport transport = new SSEHttpServletTransport(DEFAULT_MESSAGE_ENDPOINT, DEFAULT_SESSIONID_PARAMETER_NAME, asyncContext);
//		transport.setRequestTimeout(thisServer.getRequestTimeout());
		transport.setExecutor(managedExecutorService);
		sessionId = transport.getSessionId();
		MCPSession session = sessionFactory.create(transport);
		sessionManager.addSession(sessionId, session);
		if (session instanceof Server server) server.connect();
		LOGGER.info("Client Connected: " + sessionId);
		mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
	}

	/* (non-Javadoc)
	 * @see jakarta.servlet.http.HttpServlet#doPost(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String pathInfo = request.getPathInfo();
		if (!DEFAULT_MESSAGE_ENDPOINT.equals(pathInfo)) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// Get the session ID from the request parameter
		String sessionId = request.getParameter(DEFAULT_SESSIONID_PARAMETER_NAME);
		if (sessionId == null) {
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Session ID missing in message endpoint", "text/plain");
			return ;
		}
		
		if (!sessionManager.sessionExists(sessionId)) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, "Session not found: " + sessionId, "text/plain");
			return ;
		}
		
		MCPSession session = sessionManager.getSession(sessionId);  // sessions.get(sessionId);
		SSEHttpServletTransport serverTransport = (SSEHttpServletTransport) session.getTransport();
		mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
		
		String contentBody = IOUtils.toString(request.getReader());
		serverTransport.handleMessage(MCPSchema.deserializeJSONRPCMessage(contentBody));
		writeResponse(response, HttpServletResponse.SC_ACCEPTED, "Accepted", "text/plain");
	}
	
	private void writeResponse(final HttpServletResponse response, final int statusCode, final String message, final String contentType) throws IOException {
		response.setContentType(contentType);
		response.setCharacterEncoding(UTF_8);
		response.setStatus(statusCode);
		PrintWriter writer = response.getWriter();
		writer.write(message);
		writer.flush();
	}
}
