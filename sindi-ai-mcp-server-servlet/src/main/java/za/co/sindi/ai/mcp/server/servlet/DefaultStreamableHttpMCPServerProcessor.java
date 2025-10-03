package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.ErrorCodes;
import za.co.sindi.ai.mcp.schema.Implementation;
import za.co.sindi.ai.mcp.schema.JSONRPCError;
import za.co.sindi.ai.mcp.schema.JSONRPCError.Error;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.schema.JSONRPCVersion;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.ProtocolVersion;
import za.co.sindi.ai.mcp.server.EventStore;
import za.co.sindi.ai.mcp.server.McpServer;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.SessionFactory;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.streamable.SessionIdGenerator;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@ApplicationScoped
public class DefaultStreamableHttpMCPServerProcessor extends BaseHttpMCPServerProcessor implements StreamableHttpMCPServerProcessor {
	
	private static final Logger LOGGER = Logger.getLogger(DefaultStreamableHttpMCPServerProcessor.class.getName());
	
	private static final String MCP_PROTOCOL_VERSION_HTTP_HEADER_NAME = "mcp-protocol-version";
	
	protected static final String MCP_SESSION_ID_HTTP_HEADER_NAME = "mcp-session-id";
	
	protected static final String APPLICATION_JSON = "application/json";
	
	protected static final String TEXT_PLAIN = "text/plain";
	
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
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader == null  || !acceptHeader.contains("text/event-stream")) {
			writeResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept text/event-stream"));
			return ;
		}
		
		Optional<String> sessionIdOptional = validateSession(request, response);
		if (sessionIdOptional.isEmpty()) return ;
		
		if (!validateProtocolVersion(request, response)) {
			return ;
		}
		
		MCPServerSession session = sessionManager.getSession(sessionIdOptional.get());
		if (session == null) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
			return ;
		}
		
		StreamableHTTPServerTransport serverTransport = (StreamableHTTPServerTransport) session.getTransport();
		serverTransport.handleHttpGetRequest(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader == null || !acceptHeader.contains("application/json") || !acceptHeader.contains("text/event-stream")) {
			writeResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept both application/json and text/event-stream"));
			return ;
		}
		
		String contentType = request.getHeader("Content-Type");
		if (contentType == null || !contentType.contains("application/json")) {
			writeResponse(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Unsupported Media Type: ContentBlock-Type must be application/json"));
			return ;
		}
		
		StreamableHTTPServerTransport transport = null;
		String sessionId = request.getHeader(MCP_SESSION_ID_HTTP_HEADER_NAME);
		if (Strings.isNullOrEmpty(sessionId)) {
			final MCPServerSession[] sessionHolder = new MCPServerSession[1];
			sessionHolder[0] = sessionFactory.create(new StreamableHTTPServerTransport(sessionIdGenerator, false, eventStore, (String newSessionId) -> {
				LOGGER.info("Session initialized with ID: " + newSessionId);
				sessionManager.addSession(newSessionId, sessionHolder[0]);
			}));
			
			transport = (StreamableHTTPServerTransport) sessionHolder[0].getTransport();
//			transport.setRequestTimeout(thisServer.getRequestTimeout());
			transport.setExecutor(managedExecutorService);
			
			sessionHolder[0].connect();
			mcpContextFactory.getMCPContext(mcpServerConfig, (ResourceManager)mcpServer, (PromptManager)mcpServer, (ToolManager)mcpServer, sessionHolder[0]);
		} else {
			MCPServerSession session = sessionManager.getSession(sessionId);
			if (session == null) {
				writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
				return ;
			}
			
			transport = (StreamableHTTPServerTransport) session.getTransport();
		}
		
		if (transport != null) transport.handleHttpPostRequest(request, response);
	}

	@Override
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		Optional<String> sessionIdOptional = validateSession(request, response);
		if (sessionIdOptional.isEmpty()) return ;
		
		MCPServerSession session = sessionManager.getSession(sessionIdOptional.get());
		if (session == null) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
			return ;
		}
		
		session.closeQuietly();
		sessionManager.removeSession(sessionIdOptional.get());
		writeResponse(response, HttpServletResponse.SC_OK, "OK", TEXT_PLAIN);
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
		writeResponse(response, statusCode, MCPSchema.serializeJSONRPCMessage(message), APPLICATION_JSON);
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
