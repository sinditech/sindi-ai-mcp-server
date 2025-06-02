/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
import za.co.sindi.ai.mcp.schema.ServerNotification;
import za.co.sindi.ai.mcp.server.DefaultServer;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ServerFactory;
import za.co.sindi.ai.mcp.server.impl.FeatureManager;
import za.co.sindi.ai.mcp.server.mcp.scanner.ServletContextResourceContext;
import za.co.sindi.ai.mcp.server.runtime.BeanDefinitionRegistry;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultFeatureExecutorFactory;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultMCPServerConfig;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultSessionManager;
import za.co.sindi.ai.mcp.server.runtime.impl.DefaultBeanDefinitionRegistry.BeanDefinitionRegistryBuilder;
import za.co.sindi.ai.mcp.server.runtime.streamable.SessionIdGenerator;
import za.co.sindi.ai.mcp.server.spi.MCPServerConfig;
import za.co.sindi.commons.utils.Strings;
import za.co.sindi.resource.scanner.ClassScanner;
import za.co.sindi.resource.scanner.ScanningException;
import za.co.sindi.resource.scanner.impl.ResourceClassScanner;
import za.co.sindi.resource.scanner.impl.ResourceContextResourceScanner;

/**
 * @author Buhake Sindi
 * @since 25 May 2025
 */
@WebServlet(value = "/mcp", asyncSupported = true)
public class StreamableHTTPServerServlet extends HttpServlet implements MCPServerTransportProvider {
	
	private static final Logger LOGGER = Logger.getLogger(StreamableHTTPServerServlet.class.getName());
	
	private static final String DEFAULT_APPLICATION_NAME = "Java MCP Server";
	
	private static final String DEFAULT_APPLICATON_VERSION = "1.0.0-streamable";
	
	protected static final String MCP_SESSION_ID_HTTP_HEADER_NAME = "mcp-session-id";
	
	protected static final String APPLICATION_JSON = "application/json";
	
	protected static final String TEXT_PLAIN = "text/plain";
	
	protected static final String UTF_8 = "UTF-8";
	
	private final Set<String> ALLOWED_HTTP_METHODS = Set.of("DELETE", "GET", "POST");
	
	private final ThreadLocal<SessionIdGenerator> sessionIdGenerator = new ThreadLocal<>();
	
	private final SessionManager sessionManager = new DefaultSessionManager();
	
	private final HttpServletMCPServer thisServer = new HttpServletMCPServer(this); //we need an instance to this server;
	
	private ServerFactory serverFactory;
	
	private MCPServerConfig mcpServerConfig;
	
	private FeatureManager featureManager;
	
	@Resource
	private ManagedExecutorService managedExecutorService;
	
	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		if (sessionManager.totalSessions() > 0) {
			Iterator<Server> itr = sessionManager.iterator();
			while (itr.hasNext()) {
				Server server = itr.next();
				server.closeQuietly();
				itr.remove();
			}
		}
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
		CompletableFuture<Void>[] allFutures = new CompletableFuture[ sessionManager.totalSessions() /* sessions.size() */];
		int i = 0;
		for (Server server : sessionManager.getSessions()  /* sessions.values() */) {
			allFutures[i++] = server.sendNotification(notification);
		}
		return CompletableFuture.allOf(allFutures);
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		try {
			sessionIdGenerator.set(() -> UUID.randomUUID().toString());
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

	@Override
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return getClass().getName();
	}
	
	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String requestMethod = request.getMethod();
        if (!ALLOWED_HTTP_METHODS.contains(requestMethod)) {
        	writeResponse(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Method not allowed."));
//        	response.sendError(HttpServletResponse.SC_BAD_REQUEST, "HTTP request method '" + requestMethod + "' is not supported.");
        	return ;
        }
        
        super.service(req, res);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader == null  || !acceptHeader.contains("text/event-stream")) {
			writeResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept text/event-stream"));
			return ;
		}
		
		String sessionId = request.getHeader(MCP_SESSION_ID_HTTP_HEADER_NAME);
		if (Strings.isNullOrEmpty(sessionId)) {
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Bad Request: Mcp-Session-Id header is required"));
			return ;
		}
		
		Server server = sessionManager.getSession(sessionId);
		if (server == null) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
			return ;
		}
		
		StreamableHTTPServerTransport serverTransport = (StreamableHTTPServerTransport) server.getTransport();
		serverTransport.handleHttpGetRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader == null || !acceptHeader.contains("application/json") || !acceptHeader.contains("text/event-stream")) {
			writeResponse(response, HttpServletResponse.SC_NOT_ACCEPTABLE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Not Acceptable: Client must accept both application/json and text/event-stream"));
			return ;
		}
		
		String contentType = request.getHeader("Content-Type");
		if (contentType == null || !contentType.contains("application/json")) {
			writeResponse(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Unsupported Media Type: Content-Type must be application/json"));
			return ;
		}
		
		StreamableHTTPServerTransport transport = null;
		String sessionId = request.getHeader(MCP_SESSION_ID_HTTP_HEADER_NAME);
		if (Strings.isNullOrEmpty(sessionId)) {
			transport = new StreamableHTTPServerTransport(sessionIdGenerator.get(), false, null, (String newSessionId, StreamableHTTPServerTransport streamableTransport) -> { 
				Server server = serverFactory.create(streamableTransport);
				server.setCloseCallback(() -> sessionManager.removeSession(newSessionId));
				sessionManager.addSession(newSessionId, server);
				server.connect();
			});
			transport.setRequestTimeout(thisServer.getRequestTimeout());
			transport.setExecutor(managedExecutorService);
		} else {
			Server server = sessionManager.getSession(sessionId);
			if (server == null) {
				writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
				return ;
			}
			
			transport = (StreamableHTTPServerTransport) server.getTransport();
		}
		
		if (transport != null) transport.handleHttpPostRequest(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String sessionId = request.getHeader(MCP_SESSION_ID_HTTP_HEADER_NAME);
		if (Strings.isNullOrEmpty(sessionId)) {
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Bad Request: Mcp-Session-Id header is required"));
			return ;
		}
		
		Server server = sessionManager.getSession(sessionId);
		if (server == null) {
			writeResponse(response, HttpServletResponse.SC_NOT_FOUND, createJSONRPCError(ErrorCodes.REQUEST_TIMEOUT, "Session not found"));
			return ;
		}
		
		server.closeQuietly();
		sessionManager.removeSession(sessionId);
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
}
