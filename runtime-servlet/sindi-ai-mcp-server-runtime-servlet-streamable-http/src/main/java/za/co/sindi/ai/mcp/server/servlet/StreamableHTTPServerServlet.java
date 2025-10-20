/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.ErrorCodes;
import za.co.sindi.ai.mcp.schema.JSONRPCError;
import za.co.sindi.ai.mcp.schema.JSONRPCError.Error;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.schema.JSONRPCVersion;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.ProtocolVersion;
import za.co.sindi.ai.mcp.server.EventStore;
import za.co.sindi.ai.mcp.server.MCPSession;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.runtime.SessionFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.streamable.SessionIdGenerator;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 25 May 2025
 */
@WebServlet(value = "/mcp", asyncSupported = true)
public class StreamableHTTPServerServlet extends HttpServlet /* implements MCPServerTransportProvider */ {
	
	private static final Logger LOGGER = Logger.getLogger(StreamableHTTPServerServlet.class.getName());
	
	private static final String MCP_PROTOCOL_VERSION_HTTP_HEADER_NAME = "mcp-protocol-version";
	
	protected static final String MCP_SESSION_ID_HTTP_HEADER_NAME = "mcp-session-id";
	
	protected static final String APPLICATION_JSON = "application/json";
	
	protected static final String TEXT_PLAIN = "text/plain";
	
	protected static final String TEXT_EVENT_STREAM = "text/event-stream";
	
	protected static final String UTF_8 = "UTF-8";
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("DELETE", "GET", "POST");
	
	@Inject
	private SessionIdGenerator sessionIdGenerator;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private EventStore eventStore;
	
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
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return getClass().getName();
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
        String requestMethod = request.getMethod();
        if (!ALLOWED_HTTP_METHODS.contains(requestMethod)) {
        	writeResponse(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Method not allowed."));
//        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP request method '" + requestMethod + "' is not supported.");
        	return ;
        }
        
        super.service(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader == null  || !acceptHeader.contains(TEXT_EVENT_STREAM)) {
			writeResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept text/event-stream"));
			return ;
		}
		
		Optional<String> sessionIdOptional = validateSession(request, response);
		if (sessionIdOptional.isEmpty()) return ;
		
		if (!validateProtocolVersion(request, response)) {
			return ;
		}
		
		MCPSession session = sessionManager.getSession(sessionIdOptional.get());
		if (session == null) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
			return ;
		}
		
		mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
		StreamableHTTPServerTransport serverTransport = (StreamableHTTPServerTransport) session.getTransport();
		serverTransport.handleHttpGetRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader == null || !acceptHeader.contains(APPLICATION_JSON) || !acceptHeader.contains(TEXT_EVENT_STREAM)) {
			writeResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept both application/json and text/event-stream"));
			return ;
		}
		
		String contentType = request.getHeader("Content-Type");
		if (contentType == null || !contentType.contains(APPLICATION_JSON)) {
			writeResponse(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Unsupported Media Type: Content-Type must be application/json"));
			return ;
		}
		
		StreamableHTTPServerTransport transport = null;
		String sessionId = request.getHeader(MCP_SESSION_ID_HTTP_HEADER_NAME);
		if (Strings.isNullOrEmpty(sessionId)) {
			final MCPSession[] sessionHolder = new MCPSession[1];
			sessionHolder[0] = sessionFactory.create(new StreamableHTTPServerTransport(sessionIdGenerator, false, eventStore, (String newSessionId) -> {
				LOGGER.info("Session initialized with ID: " + newSessionId);
				sessionManager.addSession(newSessionId, sessionHolder[0]);
			}));
			
			transport = (StreamableHTTPServerTransport) sessionHolder[0].getTransport();
//			transport.setRequestTimeout(thisServer.getRequestTimeout());
			transport.setExecutor(managedExecutorService);
			
			if (sessionHolder[0] instanceof Server server) server.connect();
			mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, sessionHolder[0]);
		} else {
			MCPSession session = sessionManager.getSession(sessionId);
			if (session == null) {
				writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
				return ;
			}
			
			transport = (StreamableHTTPServerTransport) session.getTransport();
			mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
		}
		
		if (transport != null) transport.handleHttpPostRequest(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Optional<String> sessionIdOptional = validateSession(request, response);
		if (sessionIdOptional.isEmpty()) return ;
		
		MCPSession session = sessionManager.getSession(sessionIdOptional.get());
		if (session == null) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
			return ;
		}
		
		session.closeQuietly();
		sessionManager.removeSession(sessionIdOptional.get());
		MCPContext mcpContext = MCPContext.getCurrentInstance();
		if (mcpContext != null) mcpContext.release();
		writeResponse(response, HttpServletResponse.SC_OK, TEXT_PLAIN, "OK");
	}
	
	protected static JSONRPCError createJSONRPCError(final int errorCode, final String message) {
		return createJSONRPCError(errorCode, message, null);
	}
	
	protected static JSONRPCError createJSONRPCError(final int errorCode, final String message, final Object data) {
		JSONRPCError jsonRPCError = new JSONRPCError();
		jsonRPCError.setJsonrpc(JSONRPCVersion.getLatest());
//		jsonRPCError.setId(id);
		Error error = new Error();
		error.setCode(errorCode);
		error.setMessage(message);
		error.setData(data);
		jsonRPCError.setError(error);
		
		return jsonRPCError;
	}
	
	protected static void writeResponse(final HttpServletResponse response, final int statusCode, final JSONRPCMessage message) throws IOException {
		writeResponse(response, statusCode, APPLICATION_JSON, MCPSchema.serializeJSONRPCMessage(message));
	}
	
	protected static void writeResponse(final HttpServletResponse response, final int statusCode, final String contentType, final String message) throws IOException {
		response.setContentType(contentType);
		response.setCharacterEncoding(UTF_8);
		response.setStatus(statusCode);
		PrintWriter writer = response.getWriter();
		writer.write(message);
		writer.flush();
	}
	
	protected static Optional<String> validateSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		String sessionId = request.getHeader(MCP_SESSION_ID_HTTP_HEADER_NAME);
		if (Strings.isNullOrEmpty(sessionId)) {
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Bad Request: Mcp-Session-Id header is required"));
			return Optional.empty();
		}
		
		return Optional.of(sessionId);
	}
	
	protected static boolean validateProtocolVersion(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		String protocolVersion = request.getHeader(MCP_PROTOCOL_VERSION_HTTP_HEADER_NAME);
		try {
			ProtocolVersion.of(protocolVersion);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Bad Request: Unsupported protocol version (supported versions: " + Strings.join(", ", ProtocolVersion.values()) + ")"));
			return false;
		}
		
		return true;
	}
}
