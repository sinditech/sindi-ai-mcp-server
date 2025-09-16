package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.SessionFactory;
import za.co.sindi.ai.mcp.server.impl.MCPServerFeatureManager;
import za.co.sindi.ai.mcp.server.mcp.scanner.ServletContextResourceContext;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.MCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPContextFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultSessionManager;
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
public class SSEServerServlet extends HttpServlet /* implements MCPServerTransportProvider */ {
	
	private static final Logger LOGGER = Logger.getLogger(SSEServerServlet.class.getName());
	
	private static final String DEFAULT_APPLICATION_NAME = "Java MCP Server";
	
	private static final String DEFAULT_APPLICATON_VERSION = "1.0.0";
	
	private static final String UTF_8 = "UTF-8";
	
	/** Default endpoint path for SSE connections */
	private static final String DEFAULT_SSE_ENDPOINT = "/sse";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_MESSAGE_ENDPOINT = "/message";
	
	/** Default endpoint path for message connections */
	private static final String DEFAULT_SESSIONID_PARAMETER_NAME = "sessionId";
	
	/** Map of active client sessions, keyed by session ID */
//	private final Map<String, Server> sessions = new ConcurrentHashMap<>();
	private final SessionManager sessionManager = new DefaultSessionManager();
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("GET", "POST");
	
	private final MCPContextFactory mcpContextFactory = new DefaultMCPContextFactory();
	
	private SessionFactory sessionFactory;
	
	private MCPServerConfig mcpServerConfig;
	
	private MCPServerFeatureManager featureManager;
	
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
			featureManager = new MCPServerFeatureManager(mcpServerConfig.getCapabilities(), sessionManager, featureDefinitionManager); //new MCPServerFeatureManager(thisServer, featureDefinitionManager);
			sessionFactory = (transport) -> {
				MCPServerSession session = new MCPServerSession(transport, mcpServerConfig.getServerInfo(), mcpServerConfig.getCapabilities(), mcpServerConfig.getInstructions());
				featureManager.setup(session);
				session.setCloseCallback(() -> {
					LOGGER.info("Client Disconnected: " + transport.getSessionId());
				    sessionManager.removeSession(transport.getSessionId());
				});
				return session;
			};
		} catch (ScanningException e) {
			// TODO Auto-generated catch block
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		if (sessionManager.totalSessions() > 0) {
			Iterator<MCPServerSession> itr = sessionManager.iterator();
			while (itr.hasNext()) {
				MCPServerSession session = itr.next();
				session.closeQuietly();
				itr.remove();
			}
		}
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
		MCPServerSession session = sessionFactory.create(transport);
		sessionManager.addSession(sessionId, session);
		session.connect();
		LOGGER.info("Client Connected: " + sessionId);
		mcpContextFactory.getMCPContext(mcpServerConfig, featureManager, featureManager, featureManager, session);
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
		
		if (/*!sessions.containsKey(sessionId)*/ !sessionManager.sessionExists(sessionId)) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, "Session not found: " + sessionId, "text/plain");
			return ;
		}
		
		MCPServerSession session = sessionManager.getSession(sessionId);  // sessions.get(sessionId);
		SSEHttpServletTransport serverTransport = (SSEHttpServletTransport) session.getTransport();
		
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
