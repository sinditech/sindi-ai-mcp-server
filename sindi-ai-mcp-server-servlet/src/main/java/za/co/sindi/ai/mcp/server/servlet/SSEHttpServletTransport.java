/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletResponse;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.shared.AbstractTransport;
import za.co.sindi.ai.mcp.shared.ServerTransport;
import za.co.sindi.ai.mcp.shared.TransportException;

/**
 * @author Buhake Sindi
 * @since 05 May 2025
 */
public class SSEHttpServletTransport extends AbstractTransport implements ServerTransport {
	
	private static final Logger LOGGER = Logger.getLogger(SSEHttpServletTransport.class.getName());
	
	/** Event type for regular messages */
	private static final String MESSAGE_EVENT_TYPE = "message";

	/** Event type for endpoint information */
	private static final String ENDPOINT_EVENT_TYPE = "endpoint";
	
	private static final String APPLICATION_JSON = "application/json";
	
	private static final String TEXT_PLAIN = "text/plain";
	
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	
	private final String messageEndpoint;
	
	private final String sessionIdParameterName;
	
	private final String sessionId;
	
	private final AsyncContext asyncContext;
	
	/**
	 * @param messageEndpoint
	 * @param asyncContext
	 */
	public SSEHttpServletTransport(String messageEndpoint, AsyncContext asyncContext) {
		this(messageEndpoint, "sessionId", UUID.randomUUID().toString(), asyncContext);
	}
	
	/**
	 * @param messageEndpoint
	 * @param sessionIdParameterName
	 * @param asyncContext
	 */
	public SSEHttpServletTransport(String messageEndpoint, String sessionIdParameterName, AsyncContext asyncContext) {
		this(messageEndpoint,sessionIdParameterName, UUID.randomUUID().toString(), asyncContext);
	}

	/**
	 * @param messageEndpoint
	 * @param sessionIdParameterName
	 * @param sessionId
	 * @param asyncContext
	 */
	public SSEHttpServletTransport(String messageEndpoint, String sessionIdParameterName, String sessionId, AsyncContext asyncContext) {
		super();
		this.messageEndpoint = messageEndpoint;
		this.sessionIdParameterName = sessionIdParameterName;
		this.sessionId = sessionId;
		this.asyncContext = asyncContext;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		asyncContext.complete();
	}

	@Override
	public CompletableFuture<Void> startAsync() {
		// TODO Auto-generated method stub
		if (asyncContext == null) {
			throw new TransportException("HTTP Servlet asynchronous context hasn't been initialized!");
		}
		
		if (!initialized.compareAndSet(false, true)) {
            throw new TransportException(this.getClass().getSimpleName() + " has already started! If using Server class, note that connect() calls start() automatically.");
        }
		
		asyncContext.addListener(new AsyncListener() {
			
			@Override
			public void onTimeout(AsyncEvent event) throws IOException {
				// TODO Auto-generated method stub
				Throwable ex = event.getThrowable();
				if (ex != null) {
					LOGGER.log(Level.WARNING, "[sessionId="+sessionId+"] onTimeout() received from async event.", ex);
					getMessageHandler().onError(ex);
				}
			}
			
			@Override
			public void onStartAsync(AsyncEvent event) throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onError(AsyncEvent event) throws IOException {
				// TODO Auto-generated method stub
				Throwable ex = event.getThrowable();
				if (ex != null) {
					LOGGER.log(Level.WARNING, "[sessionId="+sessionId+"] onError() received from async event.", ex);
					getMessageHandler().onError(ex);
				}
			}
			
			@Override
			public void onComplete(AsyncEvent event) throws IOException {
				// TODO Auto-generated method stub
				
			}
		});
		
		return broadcast(TEXT_PLAIN, ENDPOINT_EVENT_TYPE, messageEndpoint + "?" + sessionIdParameterName + "=" + sessionId);
	}

	@Override
	public CompletableFuture<Void> sendAsync(JSONRPCMessage message) {
		// TODO Auto-generated method stub
		if (asyncContext == null) {
			throw new TransportException("HTTP Servlet asynchronous context hasn't been initialized!");
		}
		
		if (!initialized.get()) {
			throw new TransportException("Transport not connected.");
		}
		
		return broadcast(APPLICATION_JSON, MESSAGE_EVENT_TYPE, MCPSchema.serializeJSONRPCMessage(message));
	}
	
	public CompletableFuture<Void> handleMessage(final JSONRPCMessage message) {
		
		return CompletableFuture.runAsync(() -> getMessageHandler().onMessage(message), getExecutor());
	}
	
	private CompletableFuture<Void> broadcast(final String mediaType, final String eventType, final String data) {
		// TODO Auto-generated method stub
		return CompletableFuture.runAsync(() -> {
			try {
				HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
				PrintWriter writer = response.getWriter();
				writer.write("event: " + eventType + "\n");
				writer.write("data: " + data + "\n\n");
				
				if (writer.checkError()) {
					throw new IOException("Client disconnected");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.log(Level.WARNING, "[sessionId="+sessionId+"] Error sending async event", e);
				// Complete the async context
				asyncContext.complete();
	            
	            // Rethrow as unchecked to be handled by CompletableFuture
	            throw new CompletionException(e);
			}
		}, getExecutor());
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId;
	}
}
