package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.mapper.JSONObjectMapper;
import za.co.sindi.ai.mcp.mapper.ObjectMapper;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.ServerNotification;
import za.co.sindi.ai.mcp.server.DefaultServer;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ServerFactory;
import za.co.sindi.ai.mcp.server.impl.FeatureManager;
import za.co.sindi.ai.mcp.server.mcp.scanner.ServletContextResourceContext;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.commons.utils.IOUtils;
import za.co.sindi.resource.scanner.ClassScanner;
import za.co.sindi.resource.scanner.ScanningException;
import za.co.sindi.resource.scanner.impl.ResourceClassScanner;
import za.co.sindi.resource.scanner.impl.ResourceContextResourceScanner;

/**
 * @author Buhake Sindi
 * @since 24 March 2025
 */
@WebServlet(value = "/*", asyncSupported = true)
public class SSEServerServlet extends HttpServlet implements MCPServerTransportProvider {
	
	private static final Logger LOGGER = Logger.getLogger(SSEServerServlet.class.getName());
	
	private static final String DEFAULT_APPLICATION_NAME = "Java MCP Server";
	
	private static final String DEFAULT_APPLICATON_VERSION = "1.0.0";
	
	private static final String UTF_8 = "UTF-8";
	
	/** JSON object mapper for serialization/deserialization */
	private final ObjectMapper objectMapper = JSONObjectMapper.newInstance();
	
	/** Default endpoint path for SSE connections */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_MESSAGE_ENDPOINT = "/message";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_SESSIONID_PARAMETER_NAME = "sessionId";
	
	/** Map of active client sessions, keyed by session ID */
	private final Map<String, Server> sessions = new ConcurrentHashMap<>();
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("GET", "POST");
	
	private final HttpServletMCPServer thisServer = new HttpServletMCPServer(this); //we need an instance to this server;
	
	private ServerFactory serverFactory;
	
	private MCPServerConfig mcpServerConfig;
	
	private FeatureManager featureManager;
	
	@Resource
	private ManagedExecutorService managedExecutorService;

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String requestMethod = request.getMethod();
        if (!ALLOWED_HTTP_METHODS.contains(requestMethod)) {
        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP request method '" + requestMethod + "' is not supported.");
        	return ;
        }
        
        super.service(req, res);
	}

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return getClass().getName();
	}

	/* (non-Javadoc)
	 * @see jakarta.servlet.http.HttpServlet#init(jakarta.servlet.ServletConfig)
	 */
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		try {
			ResourceContextResourceScanner resourceScanner = new ResourceContextResourceScanner(new ServletContextResourceContext(getServletContext()));
			resourceScanner.addResourceFilter(filter -> filter.getPath().endsWith(".jar"));
			resourceScanner.addResourceFilter(filter -> filter.getPath().endsWith(".class"));
			resourceScanner.addResourcePath("/");
			Collection<za.co.sindi.resource.Resource> resources = resourceScanner.scan();
			ClassScanner classScanner = new ResourceClassScanner();
			Collection<Class<?>> classes = new LinkedHashSet<>();
			resources.stream().forEach(resource -> classes.addAll(classScanner.scan(resource)));
			
			BeanDefinitionRegistryBuilder builder = BeanDefinitionRegistry.builder();
			builder.fromClasses(classes.toArray(new Class[classes.size()]));
			
			mcpServerConfig = new DefaultMCPServerConfig(DEFAULT_APPLICATION_NAME, DEFAULT_APPLICATON_VERSION, null).enableAll();
			
			FeatureDefinitionManager featureDefinitionManager = new DefaultFeatureDefinitionManager(builder.build().getBeans(), new DefaultFeatureExecutorFactory());
			featureManager = new FeatureManager(thisServer, featureDefinitionManager);
			serverFactory = (transport) -> {
				Server server = new DefaultServer(transport, thisServer.getServerInfo(), thisServer.getServerCapabilities(), thisServer.getInstructions());
				featureManager.registerServer(server, featureDefinitionManager);
				return server;
			};
		} catch (ScanningException e) {
			// TODO Auto-generated catch block
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		thisServer.closeQuietly(); //Weird magic, but it will call the this.close(), so no stress needed here.
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
		
		response.setContentType("text/event-stream");
		response.setCharacterEncoding(UTF_8);
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		response.setHeader("Access-Control-Allow-Origin", "*");

		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);

		SSEHttpServletTransport transport = new SSEHttpServletTransport(DEFAULT_MESSAGE_ENDPOINT, DEFAULT_SESSIONID_PARAMETER_NAME, asyncContext);
		transport.setRequestTimeout(thisServer.getRequestTimeout());
		transport.setExecutor(managedExecutorService);
		String sessionId = transport.getSessionId();
		Server server = serverFactory.create(transport);
		server.setCloseCallback(() -> sessions.remove(sessionId));
		sessions.put(sessionId, server);
		server.connect();
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
		
		if (!sessions.containsKey(sessionId)) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, "Session not found: " + sessionId, "text/plain");
			return ;
		}
		
		Server server = sessions.get(sessionId);
		SSEHttpServletTransport serverTransport = (SSEHttpServletTransport) server.getTransport();
		
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

	@Override
	public MCPServerConfig getMCPServerConfig() {
		// TODO Auto-generated method stub
		return mcpServerConfig;
	}

	@Override
	public CompletableFuture<Void> notifyAllClients(ServerNotification notification) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] allFutures = new CompletableFuture[sessions.size()];
		int i = 0;
		for (Server server : sessions.values()) {
			allFutures[i++] = server.sendNotification(notification);
		}
		return CompletableFuture.allOf(allFutures);
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		if (!sessions.isEmpty()) {
			for(Server server : sessions.values()) {
				server.closeQuietly();
			}
			
			sessions.clear();
		}
	}
}
