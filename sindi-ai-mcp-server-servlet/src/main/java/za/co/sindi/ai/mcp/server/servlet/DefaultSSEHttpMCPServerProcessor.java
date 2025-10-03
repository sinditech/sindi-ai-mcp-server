package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.server.McpServer;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.SessionFactory;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.commons.utils.IOUtils;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@ApplicationScoped
public class DefaultSSEHttpMCPServerProcessor extends BaseHttpMCPServerProcessor implements SSEHttpMCPServerProcessor {
	
	private static final Logger LOGGER = Logger.getLogger(DefaultSSEHttpMCPServerProcessor.class.getName());
	
	/** Default endpoint path for SSE connections */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_MESSAGE_ENDPOINT = "/message";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_SESSIONID_PARAMETER_NAME = "sessionId";
	
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
	private McpServer mcpServer;
	
	@Resource
	private ManagedExecutorService managedExecutorService;

	@Override
	public Implementation getServerInfo() {
		// TODO Auto-generated method stub
		return mcpServerConfig.getServerInfo();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		MCPServerSession session = sessionFactory.create(transport);
		sessionManager.addSession(sessionId, session);
		session.connect();
		LOGGER.info("Client Connected: " + sessionId);
		mcpContextFactory.getMCPContext(mcpServerConfig, (ResourceManager)mcpServer, (PromptManager)mcpServer, (ToolManager)mcpServer, session);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		
		MCPServerSession session = sessionManager.getSession(sessionId);
		SSEHttpServletTransport serverTransport = (SSEHttpServletTransport) session.getTransport();
		
		String contentBody = IOUtils.toString(request.getReader());
		serverTransport.handleMessage(MCPSchema.deserializeJSONRPCMessage(contentBody));
		writeResponse(response, HttpServletResponse.SC_ACCEPTED, "Accepted", "text/plain");
	}
}
