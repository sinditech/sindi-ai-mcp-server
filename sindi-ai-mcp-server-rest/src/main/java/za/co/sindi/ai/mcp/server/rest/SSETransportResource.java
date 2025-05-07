package za.co.sindi.ai.mcp.server.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
import za.co.sindi.ai.mcp.server.BaseServer;
import za.co.sindi.ai.mcp.server.DefaultServer;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ServerFactory;
import za.co.sindi.ai.mcp.server.impl.FeatureManager;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.ai.mcp.tools.MCPCalculator;
import za.co.sindi.ai.mcp.tools.MCPRealWeather;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@ApplicationScoped
@Path("")
public class SSETransportResource extends BaseServer {
	
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
	private final Map<String, Server> sessions = new ConcurrentHashMap<>();
	
	private ServerFactory serverFactory;
	
	private MCPServerConfig mcpServerConfig;
	
	private FeatureManager featureManager;
	
	@Resource
	private ManagedExecutorService managedExecutorService;
	
	@PostConstruct
	private void init() {
		
		BeanDefinitionRegistryBuilder builder = BeanDefinitionRegistry.builder();
		builder.fromClasses(MCPCalculator.class, MCPRealWeather.class);
		
		mcpServerConfig = new DefaultMCPServerConfig(DEFAULT_APPLICATION_NAME, DEFAULT_APPLICATON_VERSION, null).enableAll();
		
		FeatureDefinitionManager featureDefinitionManager = new DefaultFeatureDefinitionManager(builder.build().getBeans(), new DefaultFeatureExecutorFactory());
		featureManager = new FeatureManager(this, featureDefinitionManager);
		serverFactory = (transport) -> {
			Server server = new DefaultServer(transport, getServerInfo(), getServerCapabilities(), getInstructions());
			featureManager.registerServer(server, featureDefinitionManager);
			return server;
		};
	}
	
	@PreDestroy 
	private void destroy() {
		closeQuietly();
	}
	
	@GET
    @Path(DEFAULT_SSE_ENDPOINT)
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void subscribeToSystem(@Context SseEventSink sink, @Context Sse sse) {
		
		SSERestServerTransport transport = new SSERestServerTransport(DEFAULT_MESSAGE_ENDPOINT, DEFAULT_SESSIONID_PARAMETER_NAME, sse, sink);
		transport.setRequestTimeout(getRequestTimeout());
		transport.setExecutor(managedExecutorService);
		String sessionId = transport.getSessionId();
		Server server = serverFactory.create(transport);
		server.setCloseCallback(() -> sessions.remove(sessionId));
		sessions.put(sessionId, server);
		server.connect();
	}
	
	@POST
    @Path(DEFAULT_MESSAGE_ENDPOINT)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response handlePostMessage(@QueryParam(DEFAULT_SESSIONID_PARAMETER_NAME) final String sessionId, JSONRPCMessage message) {
		if (!sessions.containsKey(sessionId)) {
			return Response.status(Status.NOT_FOUND).entity("Session not found: " + sessionId).build();
		}
		
		Server server = sessions.get(sessionId);
		SSERestServerTransport serverTransport = (SSERestServerTransport) server.getTransport();
		serverTransport.handleMessage(message);
		return Response.accepted("Accepted").build();
	}

	@Override
	public MCPServerConfig getMcpServerConfig() {
		// TODO Auto-generated method stub
		return mcpServerConfig;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		super.close();
		if (!sessions.isEmpty()) {
			for(Server server : sessions.values()) {
				server.closeQuietly();
			}
			
			sessions.clear();
		}
	}
}
