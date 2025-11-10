package za.co.sindi.ai.mcp.server.rest;

import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.server.MCPSession;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.MCPServer;
import za.co.sindi.ai.mcp.server.runtime.SessionFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@ApplicationScoped
@Path("")
public class SSERestServerResource {
	
	private static final Logger LOGGER = Logger.getLogger(SSERestServerResource.class.getName());
	
	/** Default endpoint path for SSE connections */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_MESSAGE_ENDPOINT = "/message";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_SESSIONID_PARAMETER_NAME = "sessionId";
	
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
	
	@GET
    @Path(DEFAULT_SSE_ENDPOINT)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribeToSystem(@Context SseEventSink sink, @Context Sse sse) {
		
		SSERestServerTransport transport = new SSERestServerTransport(DEFAULT_MESSAGE_ENDPOINT, DEFAULT_SESSIONID_PARAMETER_NAME, sse, sink);
//		transport.setRequestTimeout(getRequestTimeout());
		transport.setExecutor(managedExecutorService);
		String sessionId = transport.getSessionId();
		MCPSession session = sessionFactory.create(transport);
		sessionManager.addSession(sessionId, session);
		if (session instanceof Server server) server.connect();
		LOGGER.info("Client Connected: " + sessionId);
		mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
	}
	
	@POST
    @Path(DEFAULT_MESSAGE_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response handlePostMessage(@QueryParam(DEFAULT_SESSIONID_PARAMETER_NAME) final String sessionId, JSONRPCMessage message) {
		if (!sessionManager.sessionExists(sessionId)) {
			return Response.status(Status.NOT_FOUND).entity("Client session not found: " + sessionId).build();
		}
		
		MCPSession session = sessionManager.getSession(sessionId);
		mcpContextFactory.getMCPContext(mcpServerConfig, mcpServer, session);
		SSERestServerTransport serverTransport = (SSERestServerTransport) session.getTransport();
		serverTransport.handleMessage(message);
		return Response.accepted("Accepted").build();
	}
}
