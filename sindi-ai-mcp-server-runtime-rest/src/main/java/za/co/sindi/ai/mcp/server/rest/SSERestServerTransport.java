/**
 * 
 */
package za.co.sindi.ai.mcp.server.rest;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.shared.AbstractTransport;
import za.co.sindi.ai.mcp.shared.ServerTransport;
import za.co.sindi.ai.mcp.shared.TransportException;

/**
 * @author Buhake Sindi
 * @since 05 May 2025
 */
public class SSERestServerTransport extends AbstractTransport implements ServerTransport {
	
	private static final Logger LOGGER = Logger.getLogger(SSERestServerTransport.class.getName());
	
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
	
	private Sse sse;
	private SseEventSink sseEventSink;
	
	/**
	 * @param messageEndpoint
	 * @param sessionIdParameterName
	 * @param sse
	 * @param sseEventSink
	 */
	public SSERestServerTransport(String messageEndpoint, String sessionIdParameterName, Sse sse, SseEventSink sseEventSink) {
		this(messageEndpoint,sessionIdParameterName, UUID.randomUUID().toString(), sse, sseEventSink);
	}
	
	/**
	 * @param messageEndpoint
	 * @param sessionIdParameterName
	 * @param sessionId
	 * @param sse
	 * @param sseEventSink
	 */
	public SSERestServerTransport(String messageEndpoint, String sessionIdParameterName, String sessionId, Sse sse,
			SseEventSink sseEventSink) {
		super();
		this.messageEndpoint = messageEndpoint;
		this.sessionIdParameterName = sessionIdParameterName;
		this.sessionId = sessionId;
		this.sse = sse;
		this.sseEventSink = sseEventSink;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		sseEventSink.close();
	}

	@Override
	public CompletableFuture<Void> startAsync() {
		// TODO Auto-generated method stub
		if (sse == null || sseEventSink == null) {
			throw new TransportException("SSE session hasn't been initialized!");
		}
		
		if (!initialized.compareAndSet(false, true)) {
            throw new TransportException("SSEServerTransport already started! If using Server class, note that connect() calls start() automatically.");
        }
		
		return CompletableFuture.runAsync(() -> broadcast(TEXT_PLAIN, ENDPOINT_EVENT_TYPE, messageEndpoint + "?" + sessionIdParameterName + "=" + sessionId), getExecutor());
	}

	@Override
	public CompletableFuture<Void> sendAsync(JSONRPCMessage message) {
		// TODO Auto-generated method stub
		if (sse == null || sseEventSink == null) {
			throw new TransportException("SSE session hasn't been initialized!");
		}
		
		if (!initialized.get()) {
			throw new TransportException("Transport not connected.");
		}
		
		return CompletableFuture.runAsync(() -> broadcast(APPLICATION_JSON, MESSAGE_EVENT_TYPE, message), getExecutor());
	}
	
	public CompletableFuture<Void> handleMessage(final JSONRPCMessage message) {
		
		return CompletableFuture.runAsync(() -> getMessageHandler().onMessage(message), getExecutor());
	}

	private <T> CompletionStage<?> broadcast(final String mediaType, final String eventType, final T data) {
		// TODO Auto-generated method stub
		if (sseEventSink.isClosed()) {
			return CompletableFuture.completedStage(null);
		}
		
		MediaType _mediaType = null;
		if (TEXT_PLAIN.equals(mediaType)) _mediaType = MediaType.TEXT_PLAIN_TYPE;
		else if (APPLICATION_JSON.equals(mediaType)) _mediaType = MediaType.APPLICATION_JSON_TYPE;
		OutboundSseEvent event = sse.newEventBuilder()
									.name(eventType)
									.data(data.getClass(), data)
									.mediaType(_mediaType)
									.reconnectDelay(getRequestTimeout().toMillis())
									.build();
									
		return sseEventSink.send(event);
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId;
	}
}
