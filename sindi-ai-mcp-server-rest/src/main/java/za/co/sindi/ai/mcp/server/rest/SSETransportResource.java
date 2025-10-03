package za.co.sindi.ai.mcp.server.rest;

import java.util.Iterator;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
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
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.SessionFactory;
import za.co.sindi.ai.mcp.server.features.examples.MCPCalculator;
import za.co.sindi.ai.mcp.server.features.examples.MCPRealWeather;
import za.co.sindi.ai.mcp.server.impl.DefaultMcpServer;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultSessionManager;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@ApplicationScoped
@Path("")
public class SSETransportResource /* extends BaseServer */ {
	
	private static final Logger LOGGER = Logger.getLogger(SSETransportResource.class.getName());
	
	private static final String DEFAULT_APPLICATION_NAME = "Java MCP Server";
	
	private static final String DEFAULT_APPLICATON_VERSION = "1.0.0";
	
	/** Default endpoint path for SSE connections */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_MESSAGE_ENDPOINT = "/message";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_SESSIONID_PARAMETER_NAME = "sessionId";
	
	/** Map of active client sessions, keyed by session ID */
//	private final Map<String, Server> sessions = new ConcurrentHashMap<>();
	private final SessionManager sessionManager = new DefaultSessionManager();
	
	private SessionFactory sessionFactory;
	
	private MCPServerConfig mcpServerConfig;
	
	private DefaultMcpServer featureManager;
	
	@Resource
	private ManagedExecutorService managedExecutorService;
	
	@PostConstruct
	private void init() {
		
		BeanDefinitionRegistryBuilder builder = BeanDefinitionRegistry.builder();
		builder.fromClasses(MCPCalculator.class, MCPRealWeather.class);
		
		mcpServerConfig = new DefaultMCPServerConfig(DEFAULT_APPLICATION_NAME, DEFAULT_APPLICATON_VERSION, null).enableAll();
		
		FeatureDefinitionManager featureDefinitionManager = new DefaultFeatureDefinitionManager(builder.build().getBeans(), new DefaultFeatureExecutorFactory());
		featureManager = new DefaultMcpServer(mcpServerConfig.getCapabilities(), sessionManager, featureDefinitionManager); //new DefaultMcpServer(thisServer, featureDefinitionManager);
		sessionFactory = (transport) -> {
			MCPServerSession session = new MCPServerSession(transport, mcpServerConfig.getServerInfo(), mcpServerConfig.getCapabilities(), mcpServerConfig.getInstructions());
			featureManager.setup(session);
			session.setCloseCallback(() -> {
				LOGGER.info("Client Disconnected: " + transport.getSessionId());
			    sessionManager.removeSession(transport.getSessionId());
			});
			return session;
		};
	}
	
	@PreDestroy 
	private void destroy() {
		if (sessionManager.totalSessions() > 0) {
			Iterator<MCPServerSession> itr = sessionManager.iterator();
			while (itr.hasNext()) {
				MCPServerSession session = itr.next();
				session.closeQuietly();
				itr.remove();
			}
		}
	}
	
	@GET
    @Path(DEFAULT_SSE_ENDPOINT)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribeToSystem(@Context SseEventSink sink, @Context Sse sse) {
		
		SSERestServerTransport transport = new SSERestServerTransport(DEFAULT_MESSAGE_ENDPOINT, DEFAULT_SESSIONID_PARAMETER_NAME, sse, sink);
//		transport.setRequestTimeout(getRequestTimeout());
		transport.setExecutor(managedExecutorService);
		String sessionId = transport.getSessionId();
		MCPServerSession session = sessionFactory.create(transport);
		sessionManager.addSession(sessionId, session);
		session.connect();
		LOGGER.info("Client Connected: " + sessionId);
	}
	
	@POST
    @Path(DEFAULT_MESSAGE_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response handlePostMessage(@QueryParam(DEFAULT_SESSIONID_PARAMETER_NAME) final String sessionId, JSONRPCMessage message) {
		if (!sessionManager.sessionExists(sessionId)) {
			return Response.status(Status.NOT_FOUND).entity("Session not found: " + sessionId).build();
		}
		
		MCPServerSession session = sessionManager.getSession(sessionId);
		SSERestServerTransport serverTransport = (SSERestServerTransport) session.getTransport();
		serverTransport.handleMessage(message);
		return Response.accepted("Accepted").build();
	}
}
