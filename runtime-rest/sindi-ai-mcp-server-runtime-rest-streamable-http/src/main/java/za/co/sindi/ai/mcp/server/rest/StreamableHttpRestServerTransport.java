/**
 * 
 */
package za.co.sindi.ai.mcp.server.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.CompletionCallback;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
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
	
	/** Event type for regular messages */
	private static final String MESSAGE_EVENT_TYPE = "message";
	
	private static final StreamId STANDALONE_SSE_STREAM_ID = StreamId.of("_GET_stream");
	
	private final Map<StreamId, AsyncResponse> streamMapping = new ConcurrentHashMap<>();
	
	private final Map<RequestId, StreamId> requestToStreamMapping = new ConcurrentHashMap<>();
	  
	private final Map<RequestId, JSONRPCResponse> requestResponseMap = new ConcurrentHashMap<>();
	
	private final AtomicBoolean started = new AtomicBoolean(false);
	
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	
	private final AtomicReference<String> sessionId = new AtomicReference<>();
	
	private final SessionIdGenerator sessionIdGenerator;
	
	private final EventStore eventStore;
	
	private final SessionInitializationEvent sessionInitializationEvent;
	
//	private final Sse sse;
	
	/**
	 * @param sessionIdGenerator
	 * @param eventStore
	 * @param sessionInitializationEvent
	 */
	public StreamableHttpRestServerTransport(/* Sse sse,*/ SessionIdGenerator sessionIdGenerator, EventStore eventStore, SessionInitializationEvent sessionInitializationEvent) {
//		this.sse = sse;
		this.sessionIdGenerator = sessionIdGenerator;
		this.eventStore = eventStore;
		this.sessionInitializationEvent = sessionInitializationEvent;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
//		if (asyncContext != null) asyncContext.complete();
		streamMapping.entrySet().stream().forEach(entry -> {
			AsyncResponse asyncResponse = entry.getValue();
			if (!asyncResponse.isDone()) asyncResponse.cancel();
			LOGGER.info("[streaming ID ="+ entry.getKey().toString() +"] cancelled.");
		});
		streamMapping.clear();
		
		super.close();
		// Clear any pending responses
	    requestResponseMap.clear();
	    initialized.set(false);
	    started.set(false);
	    sessionId.set(null);
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
	public CompletableFuture<Void> sendAsync(final JSONRPCMessage message) {
		// TODO Auto-generated method stub
		return sendAsync(message, null);
	}
	
	private CompletableFuture<Void> sendAsync(final JSONRPCMessage message, final RequestId relatedRequestId) {
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
		    if (!streamMapping.containsKey(STANDALONE_SSE_STREAM_ID)) {
		    	// The spec says the server MAY send messages on the stream, so it's ok to discard if no stream
		    	return CompletableFuture.completedFuture(null);
		    }
			
			EventId eventId = null;
			if (eventStore != null) {
				eventId = eventStore.storeEvent(STANDALONE_SSE_STREAM_ID, message);
			}
			
			return broadcastMessage(streamMapping.get(STANDALONE_SSE_STREAM_ID), new JSONRPCMessage[] { message }, eventId);
		}
		
		final StreamId streamId = requestToStreamMapping.get(requestId);
		if (streamId == null) {
			throw new TransportException("No connection established for request ID: " + requestId);
		}
		
		final AsyncResponse asyncResponse = streamMapping.get(streamId);
		EventId eventId = null;
		if (eventStore != null) {
			eventId = eventStore.storeEvent(streamId, message); //.get();
		}
		
		if (asyncResponse != null) {
			broadcastMessage(asyncResponse, new JSONRPCMessage[] { message }, eventId);
		}
		
		CompletableFuture<Void> responseFuture = null;
		if (message instanceof JSONRPCResponse jsonRpcResponse) {
			requestResponseMap.put(requestId, jsonRpcResponse);
			List<RequestId> relatedIds = requestToStreamMapping.entrySet().stream().filter(entry -> streamMapping.containsKey(entry.getValue()) && streamMapping.get(entry.getValue()).equals(asyncResponse)).map(entry -> entry.getKey()).toList();
			boolean allResponsesReady = relatedIds.stream().allMatch(id -> requestResponseMap.containsKey(id));
			if (allResponsesReady) {
		        if (asyncResponse == null) {
		          throw new TransportException("No async response established for request ID: "+ requestId);
		        }
		        
		        JSONRPCMessage[] messages = relatedIds.stream().map(id -> requestResponseMap.get(id)).toArray(size -> new JSONRPCMessage[size]);
		        responseFuture = broadcastMessage(asyncResponse, messages, eventId);
		        
		        //Cleanup
		        relatedIds.stream().forEach(id -> {
		        	requestResponseMap.remove(id);
		            requestToStreamMapping.remove(id);
		        });
			}
		}
		
		return responseFuture != null ? responseFuture : CompletableFuture.completedFuture(null);
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId.get();
	}
	
	public Response handleHttpGetRequest(final AsyncResponse asyncResponse, final EventId lastEventId) {
		if (eventStore != null && lastEventId != null) {
			return replayEvents(asyncResponse, lastEventId);
		}
		
		// Check if there's already an active standalone SSE stream for this session
		if (streamMapping.containsKey(STANDALONE_SSE_STREAM_ID)) {
		  // Only one GET SSE stream is allowed per session
			throw new WebApplicationException(StreamableHttpServerResource.toResponse(Status.CONFLICT, StreamableHttpServerResource.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Conflict: Only one SSE stream is allowed per session")));
		}
		
		asyncResponse.register(new CompletionCallback() {

			@Override
			public void onComplete(Throwable throwable) {
				// TODO Auto-generated method stub
				// Set up close handler for client disconnects
				streamMapping.remove(STANDALONE_SSE_STREAM_ID);
			}
			
		});
		
		// Assign the response to the standalone SSE stream
		streamMapping.put(STANDALONE_SSE_STREAM_ID, asyncResponse);
		
//		return Response.ok().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).type(MediaType.SERVER_SENT_EVENTS_TYPE).build();
		ResponseBuilder builder = Response.ok();
		applySSEEventStreamHeaders(builder);
		return builder.build();
	}
	
	private Response replayEvents(final AsyncResponse asyncResponse, final EventId lastEventId) {
		if (eventStore == null) return Response.noContent().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).build();
				
		asyncResponse.register(new CompletionCallback() {

			@Override
			public void onComplete(Throwable throwable) {
				// TODO Auto-generated method stub
				if (throwable != null) {
					LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onError() received from async event.", throwable);
					getMessageHandler().onError(throwable);
				}
			}
		});
		
		Sender sender = (eventId, message) -> {
			broadcastMessage(asyncResponse, new JSONRPCMessage[] { message }, eventId);
		};
		
		final StreamId streamId = eventStore.replayEventAfter(lastEventId, sender); //.get();
		streamMapping.put(streamId, asyncResponse);
		
//		return Response.ok().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).build();
		ResponseBuilder builder = Response.ok();
		applySSEEventStreamHeaders(builder);
		return builder.build();
	}
	
	public Response handleHttpPostRequest(final AsyncResponse asyncResponse, final String mcpProtocolVersion, final String jsonRPCMessageStr) {
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
			StreamableHttpServerResource.validateProtocolVersion(mcpProtocolVersion);
		}
		
		boolean hasRequests = Arrays.stream(messages).anyMatch(message -> message instanceof JSONRPCRequest);
		if (!hasRequests) {
			Response accepted = Response.accepted().build();
			asyncResponse.resume(accepted);
			Arrays.stream(messages).forEach(message -> getMessageHandler().onMessage(message));
			return accepted;
		} else {
			StreamId streamId = StreamId.of(UUID.randomUUID().toString());
			asyncResponse.register(new CompletionCallback() {
	
				@Override
				public void onComplete(Throwable throwable) {
					// TODO Auto-generated method stub
					if (throwable != null) {
						LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onError() received from async event.", throwable);
						getMessageHandler().onError(throwable);
					} else {
						// Set up close handler for client disconnects
						streamMapping.remove(streamId);
					}
				}
				
			});
			//So, we have hasRequests
			
			Arrays.stream(messages).forEach(message -> {
				if (message instanceof JSONRPCRequest jsonRPCRequest) {
					streamMapping.put(streamId, asyncResponse);
					requestToStreamMapping.put(jsonRPCRequest.getId(), streamId);
				}
				
				getMessageHandler().onMessage(message);
			});
			
			ResponseBuilder builder = Response.ok();
			applySSEEventStreamHeaders(builder);
			return builder.build();
		}
		
//		return Response.ok().header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, getSessionId()).type(MediaType.SERVER_SENT_EVENTS_TYPE).build();
	}
	
	private StreamingOutput createSSEEventStream(final JSONRPCMessage[] messages, final EventId eventId) throws IOException {
		// TODO Auto-generated method stub
		
		final StreamingOutput stream = new StreamingOutput() {

			@Override
			public void write(OutputStream outputStream) throws IOException, WebApplicationException {
				// TODO Auto-generated method stub
				Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
				
				try {
					writer.write("event: " + MESSAGE_EVENT_TYPE + "\n");
					if (eventId != null) {
						writer.write("id: " + eventId + "\n");
					}
					writer.write("data: " + MCPSchema.serializeJSONRPCMessage(messages) + "\n\n");
					writer.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] Error sending async event", e);
		            
		            // Rethrow as unchecked to be handled by CompletableFuture
		            throw e;
				}
			}
		};
		
		return stream;
	}
	
	private void applySSEEventStreamHeaders(final ResponseBuilder builder) {
		builder.type(MediaType.SERVER_SENT_EVENTS_TYPE)
			   .header("Cache-Control", "no-cache, no-transform")
			   .header("Connection", "keep-alive");
		
		String mcpSessionId = getSessionId();
		if (!Strings.isNullOrEmpty(mcpSessionId)) {
			builder.header(StreamableHttpServerResource.HTTP_HEADER_MCP_SESSION_ID_NAME, mcpSessionId);
		}
	}
	
	private CompletableFuture<Void> broadcastMessage(final AsyncResponse asyncResponse, final JSONRPCMessage[] messages, final EventId eventId) {
		// TODO Auto-generated method stub
		return CompletableFuture.runAsync(() -> {
			try {
				ResponseBuilder builder = Response.ok();
				applySSEEventStreamHeaders(builder);
				Response response = builder.entity(createSSEEventStream(messages, eventId)).build();
				asyncResponse.resume(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new UncheckedIOException(e);
			}
		}, getExecutor());
	}
}
