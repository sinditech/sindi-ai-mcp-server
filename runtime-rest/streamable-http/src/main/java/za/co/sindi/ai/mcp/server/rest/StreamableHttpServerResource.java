package za.co.sindi.ai.mcp.server.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import za.co.sindi.ai.mcp.schema.ErrorCodes;
import za.co.sindi.ai.mcp.schema.JSONRPCError;
import za.co.sindi.ai.mcp.schema.JSONRPCError.Error;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.schema.JSONRPCVersion;
import za.co.sindi.ai.mcp.schema.ProtocolVersion;
import za.co.sindi.ai.mcp.server.EventId;
import za.co.sindi.ai.mcp.server.EventStore;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPSession;
import za.co.sindi.ai.mcp.server.runtime.SessionFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPContext;
import za.co.sindi.ai.mcp.server.runtime.streamable.SessionIdGenerator;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@ApplicationScoped
@Path("/mcp")
public class StreamableHttpServerResource {
	
	private static final Logger LOGGER = Logger.getLogger(StreamableHttpServerResource.class.getName());
	
	private static final String HTTP_HEADER_MCP_PROTOCOL_VERSION_NAME = "Mcp-Protocol-Version";
	
	protected static final String HTTP_HEADER_MCP_SESSION_ID_NAME = "Mcp-Session-Id";
	
	protected static final String APPLICATION_JSON = MediaType.APPLICATION_JSON;  // "application/json";
	
	protected static final String TEXT_PLAIN = MediaType.TEXT_PLAIN; // "text/plain";
	
	protected static final String TEXT_EVENT_STREAM = MediaType.SERVER_SENT_EVENTS; // "text/event-stream";
	
	protected static final String UTF_8 = "UTF-8";
	
	@Inject
	private SessionIdGenerator sessionIdGenerator;
	
	@Inject
	private EventStore eventStore;
	
	@Inject
	private SessionManager sessionManager;
	
//	@Inject
//	private MCPContextFactory mcpContextFactory;
	
	@Inject
	private SessionFactory sessionFactory;
	
//	@Inject
//	private MCPServerConfig mcpServerConfig;
	
//	@Inject
//	private MCPServer mcpServer;
	
	@Resource
	private ManagedExecutorService managedExecutorService;
	
	@GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Response getSSEStreams(@Suspended AsyncResponse asyncResponse,
    		@HeaderParam(HttpHeaders.ACCEPT) String acceptHeader,
            @HeaderParam(HTTP_HEADER_MCP_SESSION_ID_NAME) String mcpSessionId,
            @HeaderParam(HTTP_HEADER_MCP_PROTOCOL_VERSION_NAME) String mcpProtocolVersion,
            @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) String lastEventId) {
		
		validateAcceptHeader(acceptHeader, TEXT_EVENT_STREAM);
		validateMcpSession(mcpSessionId);
		validateProtocolVersion(mcpProtocolVersion);
		
		MCPSession session = sessionManager.getSession(mcpSessionId);
		if (session == null) {
			throw new NotFoundException(toResponse(Status.NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found.")));
		}
		
//		mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
		((DefaultMCPContext)MCPContext.getCurrentInstance()).setCurrentSession(session);
		StreamableHttpRestServerTransport serverTransport = (StreamableHttpRestServerTransport) session.getTransport();
		return serverTransport.handleHttpGetRequest(asyncResponse, Strings.isNullOrEmpty(lastEventId) ? null : EventId.of(lastEventId));
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON, MediaType.SERVER_SENT_EVENTS})
	public Response handlePostMessage(String jsonRPCMessageStr,
									@HeaderParam(HttpHeaders.ACCEPT) String acceptHeader,
									@HeaderParam(HttpHeaders.CONTENT_TYPE) String contentTypeHeader,
						            @HeaderParam(HTTP_HEADER_MCP_SESSION_ID_NAME) String mcpSessionId,
						            @HeaderParam(HTTP_HEADER_MCP_PROTOCOL_VERSION_NAME) String mcpProtocolVersion,
						            @Suspended AsyncResponse asyncResponse) {
		validateAcceptHeader(acceptHeader, APPLICATION_JSON, TEXT_EVENT_STREAM);
		validateContentTypeHeader(contentTypeHeader);
		
		StreamableHttpRestServerTransport transport = null;
		if (Strings.isNullOrEmpty(mcpSessionId)) {
			final MCPSession[] sessionHolder = new MCPSession[1];
			sessionHolder[0] = sessionFactory.create(new StreamableHttpRestServerTransport(sessionIdGenerator, eventStore, (String newSessionId) -> {
				LOGGER.info("Session initialized with ID: " + newSessionId);
				sessionManager.addSession(newSessionId, sessionHolder[0]);
			}));
			
			transport = (StreamableHttpRestServerTransport) sessionHolder[0].getTransport();
//			transport.setRequestTimeout(thisServer.getRequestTimeout());
			transport.setExecutor(managedExecutorService);
			
			if (sessionHolder[0] instanceof Server server) server.connect();
//			mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, sessionHolder[0]);
			((DefaultMCPContext)MCPContext.getCurrentInstance()).setCurrentSession(sessionHolder[0]);
		} else {
			MCPSession session = sessionManager.getSession(mcpSessionId);
			if (session == null) {
				throw new NotFoundException(toResponse(Status.NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found.")));
			}
			
			transport = (StreamableHttpRestServerTransport) session.getTransport();
//			mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
			((DefaultMCPContext)MCPContext.getCurrentInstance()).setCurrentSession(session);
		}
		
		asyncResponse.setTimeoutHandler(null);
		return transport.handleHttpPostRequest(asyncResponse, mcpProtocolVersion, jsonRPCMessageStr);
	}
	
	/**
     * Handles DELETE requests to terminate a session.
     */
    @DELETE
    public Response deleteSession(@HeaderParam(HTTP_HEADER_MCP_SESSION_ID_NAME) String mcpSessionId) {
    	validateMcpSession(mcpSessionId);

    	MCPSession session = sessionManager.getSession(mcpSessionId);
		if (session == null) {
			throw new NotFoundException(toResponse(Status.NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found.")));
		}
		
		session.closeQuietly();
		sessionManager.removeSession(mcpSessionId);
		MCPContext mcpContext = MCPContext.getCurrentInstance();
		if (mcpContext != null) mcpContext.release();

		return Response.ok("OK").type(MediaType.TEXT_PLAIN_TYPE).encoding(UTF_8).build();
    }
	
	private void validateAcceptHeader(final String acceptValue, final String... acceptableValues) {
		// TODO Auto-generated method stub
		Set<String> values = new HashSet<>(Arrays.asList(acceptValue.split("\\s*,\\s*")));
		boolean valueContains = Arrays.stream(acceptableValues).anyMatch(value -> values.contains(value));
		if (!valueContains) {
			throw new NotAcceptableException(toResponse(Status.NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept the following value(s): " + String.join(",", acceptableValues))));
		}
	}
	
	private void validateContentTypeHeader(final String contentType) {
		// TODO Auto-generated method stub
		if (!APPLICATION_JSON.equalsIgnoreCase(contentType)) {
			throw new NotSupportedException(toResponse(Status.UNSUPPORTED_MEDIA_TYPE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Unsupported Media Type: Content-Type must be application/json")));
		}
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
	
	protected static Response toResponse(final Status status, final JSONRPCMessage message) {
		return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).encoding(UTF_8).entity(message).build();
	}
	
	protected static Response toResponse(final Status status, final MediaType mediaType, final String message) throws IOException {
		return Response.status(status).type(mediaType).encoding(UTF_8).entity(message).build();
	}
	
	protected static void validateMcpSession(final String mcpSessionId) {
		// TODO Auto-generated method stub
		if (Strings.isNullOrEmpty(mcpSessionId)) {
			throw new BadRequestException(toResponse(Status.BAD_REQUEST, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Bad Request: Mcp-Session-Id header is required")));
		}
	}
	
	protected static void validateProtocolVersion(final String mcpProtocolVersion) {
		// TODO Auto-generated method stub
		try {
			ProtocolVersion.of(mcpProtocolVersion);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			throw new BadRequestException(toResponse(Status.BAD_REQUEST, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Bad Request: Unsupported protocol version (supported versions: " + Strings.join(", ", ProtocolVersion.values()) + ")")));
		}
	}
}
