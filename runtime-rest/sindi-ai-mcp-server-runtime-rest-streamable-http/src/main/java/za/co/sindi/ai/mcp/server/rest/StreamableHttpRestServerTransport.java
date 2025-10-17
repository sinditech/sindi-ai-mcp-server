/**
 * 
 */
package za.co.sindi.ai.mcp.server.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import za.co.sindi.ai.mcp.schema.ErrorCodes;
import za.co.sindi.ai.mcp.schema.InitializeRequest;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.schema.JSONRPCRequest;
import za.co.sindi.ai.mcp.schema.JSONRPCResponse;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.server.EventId;
import za.co.sindi.ai.mcp.server.EventStore;
import za.co.sindi.ai.mcp.server.EventStore.Sender;
import za.co.sindi.ai.mcp.server.SessionInitializationEvent;
import za.co.sindi.ai.mcp.server.StreamId;
import za.co.sindi.ai.mcp.server.runtime.streamable.SessionIdGenerator;
import za.co.sindi.ai.mcp.shared.AbstractTransport;
import za.co.sindi.ai.mcp.shared.ServerTransport;
import za.co.sindi.ai.mcp.shared.TransportException;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 16 October 2025
 */
public class StreamableHttpRestServerTransport extends AbstractTransport implements ServerTransport {
	
	private static final Logger LOGGER = Logger.getLogger(StreamableHttpRestServerTransport.class.getName());
	
	private static final StreamId STANDALONE_SSE_STREAM_ID = StreamId.of("_GET_stream");
	
	private final Map<StreamId, SseBroadcaster> streamMapping = new ConcurrentHashMap<>();
	
	private final Map<RequestId, StreamId> requestToStreamMapping = new ConcurrentHashMap<>();
	  
	private final Map<RequestId, JSONRPCResponse> requestResponseMap = new ConcurrentHashMap<>();
	
	private final AtomicBoolean started = new AtomicBoolean(false);
	
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	
	private final AtomicReference<String> sessionId = new AtomicReference<>();
	
	private final SessionIdGenerator sessionIdGenerator;
	
	private final EventStore eventStore;
	
	private final SessionInitializationEvent sessionInitializationEvent;
	
	private final Sse sse;
	
	/**
	 * @param sse
	 * @param sessionIdGenerator
	 * @param eventStore
	 * @param sessionInitializationEvent
	 */
	public StreamableHttpRestServerTransport(Sse sse, SessionIdGenerator sessionIdGenerator, EventStore eventStore, SessionInitializationEvent sessionInitializationEvent) {
		this.sse = sse;
		this.sessionIdGenerator = sessionIdGenerator;
		this.eventStore = eventStore;
		this.sessionInitializationEvent = sessionInitializationEvent;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
//		if (asyncContext != null) asyncContext.complete();
		streamMapping.values().stream().forEach(sseBroadcaster -> {
			sseBroadcaster.close();
			LOGGER.info("[sessionId="+ getSessionId() +"] closed");
		});
		streamMapping.clear();
		
		// Clear any pending responses
	    requestResponseMap.clear();
	    initialized.set(false);
	    started.set(false);
	    super.close();
	}

	@Override
	public CompletableFuture<Void> startAsync() {
		// TODO Auto-generated method stub
		if (!started.compareAndSet(false, true)) {
            throw new TransportException(this.getClass().getSimpleName() + " has already started! If using Server class, note that connect() calls start() automatically.");
        }
		
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> sendAsync(JSONRPCMessage message) {
		// TODO Auto-generated method stub
		return sendAsync(message, null);
	}
	
	private CompletableFuture<Void> sendAsync(JSONRPCMessage message, final RequestId relatedRequestId) {
		if (!initialized.get()) {
			throw new TransportException("Transport not connected.");
		}
		
		RequestId requestId = relatedRequestId;
		if (message instanceof JSONRPCResponse jsonRpcResponse) {
			requestId = jsonRpcResponse.getId();
		}
		
		if (requestId == null) {
			if (message instanceof JSONRPCResponse) {
				throw new TransportException("Cannot send a response on a standalone SSE stream unless resuming a previous client request.");
			}
			
			// Check if there's already an active standalone SSE stream for this session
		    if (streamMapping.containsKey(STANDALONE_SSE_STREAM_ID)) {
		    	// The spec says the server MAY send messages on the stream, so it's ok to discard if no stream
		    	return CompletableFuture.completedFuture(null);
		    }
			
			EventId eventId = null;
			if (eventStore != null) {
				eventId = eventStore.storeEvent(STANDALONE_SSE_STREAM_ID, message);
			}
			
			return broadcastMessage(streamMapping.get(STANDALONE_SSE_STREAM_ID), new JSONRPCMessage[] { message }, eventId).thenAccept((completion) -> {}).toCompletableFuture();
		}
		
		final StreamId streamId = requestToStreamMapping.get(requestId);
		if (streamId == null) {
			throw new TransportException("No connection established for request ID: " + requestId);
		}
		
		final SseBroadcaster sseBroadcaster = streamMapping.get(streamId);
		EventId eventId = null;
		if (eventStore != null) {
			eventId = eventStore.storeEvent(streamId, message); //.get();
		}
		
		if (sseBroadcaster != null) {
			broadcastMessage(sseBroadcaster, new JSONRPCMessage[] { message }, eventId);
		}
		
		CompletableFuture<Void> responseFuture = null;
		if (message instanceof JSONRPCResponse jsonRpcResponse) {
			requestResponseMap.put(requestId, jsonRpcResponse);
			List<RequestId> relatedIds = requestToStreamMapping.entrySet().stream().filter(entry -> streamMapping.containsKey(entry.getValue()) && streamMapping.get(entry.getValue()).equals(sseBroadcaster)).map(entry -> entry.getKey()).toList();
			boolean allResponsesReady = relatedIds.stream().allMatch(id -> requestResponseMap.containsKey(id));
			if (allResponsesReady) {
		        if (sseBroadcaster == null) {
		          throw new TransportException("No SSE broadcaster established for request ID: "+ requestId);
		        }
		        
		        JSONRPCMessage[] messages = relatedIds.stream().map(id -> requestResponseMap.get(id)).toArray(size -> new JSONRPCMessage[size]);
		        responseFuture = broadcastMessage(sseBroadcaster, messages, eventId).thenAccept((completion) -> {}).toCompletableFuture();
		        
		        //Cleanup
		        relatedIds.stream().forEach(id -> {
		        	requestResponseMap.remove(id);
		            requestToStreamMapping.remove(id);
		        });
			}
		}
		
		return responseFuture != null ? responseFuture : CompletableFuture.completedFuture(null);
	}
	
	private CompletionStage<?> broadcastMessage(final SseBroadcaster sseBroadcaster, final JSONRPCMessage[] messages, final EventId eventId) {
		// TODO Auto-generated method stub
		OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder()
				.id(eventId.toString())
				.name("message")
				.mediaType(MediaType.APPLICATION_JSON_TYPE)
				.reconnectDelay(getRequestTimeout().toMillis());
		
		if (messages.length == 1) {
			eventBuilder = eventBuilder.data(messages[0].getClass(), messages[0]);
		} else {
			eventBuilder = eventBuilder.data(messages.getClass(), messages);
		}
		
		if (eventId != null) {
			eventBuilder = eventBuilder.id(eventId.toString());
		}
		
		return sseBroadcaster.broadcast(eventBuilder.build());
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId.get();
	}
	
	public Response handleHttpGetRequest(final SseEventSink sink, final EventId lastEventId) {
		if (eventStore != null && lastEventId != null) {
			return replayEvents(lastEventId, sink);
		}
		
		// Check if there's already an active standalone SSE stream for this session
		if (streamMapping.containsKey(STANDALONE_SSE_STREAM_ID)) {
		  // Only one GET SSE stream is allowed per session
			throw new WebApplicationException(StreamableHttpServerResource.toResponse(Status.CONFLICT, StreamableHttpServerResource.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Conflict: Only one SSE stream is allowed per session")));
		}
		
		SseBroadcaster sseBroadcaster = sse.newBroadcaster();
		sseBroadcaster.onClose((sseEventSink) -> {
			// Set up close handler for client disconnects
			streamMapping.remove(STANDALONE_SSE_STREAM_ID);
		});
		
		sseBroadcaster.onError((sseEventSink, error) -> {
			if (error != null) {
				LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onError() received from async event.", error);
				getMessageHandler().onError(error);
			}
		});
		
		// Assign the response to the standalone SSE stream
		streamMapping.put(STANDALONE_SSE_STREAM_ID, sseBroadcaster);
		
		return Response.ok().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).build();
	}
	
	private Response replayEvents(final EventId lastEventId, final SseEventSink sink) {
		if (eventStore == null) return Response.noContent().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).build();
		
		SseBroadcaster sseBroadcaster = sse.newBroadcaster();
		Sender sender = (eventId, message) -> {
			sseBroadcaster.onError((sseEventSink, error) -> {
				if (error != null) {
					LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onError() received from async event.", error);
					getMessageHandler().onError(error);
					sseEventSink.close();
				}
			});
		};
		
		final StreamId streamId = eventStore.replayEventAfter(lastEventId, sender); //.get();
		streamMapping.put(streamId, sseBroadcaster);
		
		return Response.ok().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).build();
	}
	
	public Response handleHttpPostRequest(final SseEventSink sink, final String jsonRPCMessageStr) {
		JSONRPCMessage[] messages = MCPSchema.deserializeJSONRPCMessages(jsonRPCMessageStr);
		boolean isInitializationRequest = Arrays.stream(messages).anyMatch(message -> (message instanceof JSONRPCRequest && ((JSONRPCRequest)message).getMethod().equals(InitializeRequest.METHOD_INITIALIZE)));
		
		String mcpSessionId = getSessionId();
		if (isInitializationRequest) {
			if (initialized.get() && !Strings.isNullOrEmpty(mcpSessionId)) {
				//Server was already initalised....
				throw new BadRequestException(StreamableHttpServerResource.toResponse(Status.BAD_REQUEST, StreamableHttpServerResource.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Invalid Request: Server already initialized.")));
			}
			
			if (messages.length > 1) {
				throw new BadRequestException(StreamableHttpServerResource.toResponse(Status.BAD_REQUEST, StreamableHttpServerResource.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Invalid Request: Only one initialization request is allowed.")));
			}
			
			if (initialized.compareAndSet(false, true)) {
				mcpSessionId = sessionIdGenerator.generate();
				sessionId.set(mcpSessionId);
				if (sessionInitializationEvent != null) sessionInitializationEvent.onSessionInitialized(mcpSessionId);
			}
		}
		
		if (!isInitializationRequest) {
			// If an Mcp-Session-Id is returned by the server during initialization,
			// clients using the Streamable HTTP transport MUST include it 
			// in the Mcp-Session-Id header on all of their subsequent HTTP requests.
			mcpSessionId = getSessionId();
			StreamableHttpServerResource.validateMcpSession(mcpSessionId);
		}
		
		boolean hasRequests = Arrays.stream(messages).anyMatch(message -> message instanceof JSONRPCRequest);
		if (!hasRequests) {
			Arrays.stream(messages).forEach(message -> getMessageHandler().onMessage(message));
			return Response.accepted().build();
		}
		
		//So, we have hasRequests
		SseBroadcaster sseBroadcaster = sse.newBroadcaster();
		sseBroadcaster.onClose((sseEventSink) -> {
			// Set up close handler for client disconnects
			streamMapping.remove(STANDALONE_SSE_STREAM_ID);
		});
		
		sseBroadcaster.onError((sseEventSink, error) -> {
			if (error != null) {
				LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onError() received from async event.", error);
				getMessageHandler().onError(error);
			}
		});
		
		StreamId streamId = StreamId.of(UUID.randomUUID().toString());
		Arrays.stream(messages).forEach(message -> {
			if (message instanceof JSONRPCRequest jsonRPCRequest) {
				streamMapping.put(streamId, sseBroadcaster);
				requestToStreamMapping.put(jsonRPCRequest.getId(), streamId);
			}
			
			getMessageHandler().onMessage(message);
		});
		
		return Response.ok().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).build();
	}
}
