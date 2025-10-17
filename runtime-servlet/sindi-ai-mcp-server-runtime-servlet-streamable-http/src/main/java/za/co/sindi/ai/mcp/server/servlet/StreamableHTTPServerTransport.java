/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import za.co.sindi.commons.utils.IOUtils;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 05 May 2025
 */
public class StreamableHTTPServerTransport extends AbstractTransport implements ServerTransport {
	
	private static final Logger LOGGER = Logger.getLogger(StreamableHTTPServerTransport.class.getName());
	
	/** Event type for regular messages */
	private static final String MESSAGE_EVENT_TYPE = "message";
	
	private static final StreamId STANDALONE_SSE_STREAM_ID = StreamId.of("_GET_stream");
	
	private final Map<StreamId, HttpServletResponse> streamMapping = new ConcurrentHashMap<>();
	
	private final Map<RequestId, StreamId> requestToStreamMapping = new ConcurrentHashMap<>();
	  
	private final Map<RequestId, JSONRPCResponse> requestResponseMap = new ConcurrentHashMap<>();
	
	private final AtomicBoolean started = new AtomicBoolean(false);
	
	private final AtomicBoolean initialized = new AtomicBoolean(false);
	
	private final AtomicReference<String> sessionId = new AtomicReference<>();
	
	private final SessionIdGenerator sessionIdGenerator;
	
	private final boolean enableJsonResponse;
	
	private final EventStore eventStore;
	
	private final SessionInitializationEvent sessionInitializationEvent;
	
//	private AsyncContext asyncContext;
	
	/**
	 * @param sessionIdGenerator
	 * @param enableJsonResponse
	 * @param eventStore
	 * @param sessionInitializationEvent
	 */
	public StreamableHTTPServerTransport(SessionIdGenerator sessionIdGenerator, boolean enableJsonResponse,
			EventStore eventStore, SessionInitializationEvent sessionInitializationEvent) {
		super();
		this.sessionIdGenerator = sessionIdGenerator;
		this.enableJsonResponse = enableJsonResponse;
		this.eventStore = eventStore;
		this.sessionInitializationEvent = sessionInitializationEvent;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
//		if (asyncContext != null) asyncContext.complete();
		streamMapping.values().stream().forEach(response -> {
			try {
				response.flushBuffer();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] flushing response stream", e);
			}
		});
		streamMapping.clear();
		
		// Clear any pending responses
	    requestResponseMap.clear();
	    sessionId.set(null);
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
		
		return CompletableFuture.runAsync(() -> {
			try {
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
				    	return ;
				    }
					
					EventId eventId = null;
					if (eventStore != null) {
						eventId = eventStore.storeEvent(STANDALONE_SSE_STREAM_ID, message); //.get();
					}
					
					writeSSEEvent(streamMapping.get(STANDALONE_SSE_STREAM_ID), message, eventId);
					return ;
				}
				
				final StreamId streamId = requestToStreamMapping.get(requestId);
				if (streamId == null) {
					throw new TransportException("No connection established for request ID: " + requestId);
				}
				
				final HttpServletResponse response = streamMapping.get(streamId);
				if (!enableJsonResponse) {
					EventId eventId = null;
					if (eventStore != null) {
						eventId = eventStore.storeEvent(streamId, message); //.get();
					}
					
					if (response != null) {
						 writeSSEEvent(response, message, eventId);
					}
				}
				
				if (message instanceof JSONRPCResponse jsonRpcResponse) {
					requestResponseMap.put(requestId, jsonRpcResponse);
					List<RequestId> relatedIds = requestToStreamMapping.entrySet().stream().filter(entry -> streamMapping.containsKey(entry.getValue()) && streamMapping.get(entry.getValue()).equals(response)).map(entry -> entry.getKey()).toList();
					boolean allResponsesReady = relatedIds.stream().allMatch(id -> requestResponseMap.containsKey(id));
					if (allResponsesReady) {
				        if (response == null) {
				          throw new TransportException("No connection established for request ID: "+ requestId);
				        }
				        
				        if (enableJsonResponse) {
				        	response.setContentType(StreamableHTTPServerServlet.APPLICATION_JSON);
				    		response.setCharacterEncoding(StreamableHTTPServerServlet.UTF_8);
				    		
				    		if (!Strings.isNullOrEmpty(getSessionId())) {
				    			response.setHeader(StreamableHTTPServerServlet.MCP_SESSION_ID_HTTP_HEADER_NAME, getSessionId());
				    		}
				    		
				    		JSONRPCMessage[] messages = relatedIds.stream().map(id -> requestResponseMap.get(id)).toArray(size -> new JSONRPCMessage[size]);
				    		response.setStatus(HttpServletResponse.SC_OK);
				    		PrintWriter writer = response.getWriter();
				    		writer.write(messages.length == 1 ? MCPSchema.serializeJSONRPCMessage(messages[0]) : MCPSchema.serializeJSONRPCMessage(messages));
				    		writer.flush();
				        }
				        
				        //Cleanup
				        relatedIds.stream().forEach(id -> {
				        	requestResponseMap.remove(id);
				            requestToStreamMapping.remove(id);
				        });
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new CompletionException("Error sending messages.", e);
			}
		}, getExecutor());
	}
	
	private void writeSSEEvent(final HttpServletResponse response, final JSONRPCMessage message, final EventId eventId) throws IOException {
		// TODO Auto-generated method stub
		try {
			PrintWriter writer = response.getWriter();
			writer.write("event: " + MESSAGE_EVENT_TYPE + "\n");
			if (eventId != null) {
				writer.write("id: " + eventId + "\n");
			}
			writer.write("data: " + MCPSchema.serializeJSONRPCMessage(message) + "\n\n");
			
			if (writer.checkError()) {
				throw new IOException("Client disconnected");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] Error sending async event", e);
            
            // Rethrow as unchecked to be handled by CompletableFuture
            throw e;
		}
	}

	@Override
	public String getSessionId() {
		// TODO Auto-generated method stub
		return sessionId.get();
	}
	
	public void handleHttpGetRequest(final HttpServletRequest request, final HttpServletResponse response) {
		if (eventStore != null) {
			String lastEventId = request.getHeader("last-event-id");
			if (!Strings.isNullOrEmpty(lastEventId)) {
				replayEvents(EventId.of(lastEventId), request, response);
			}
		}
		
		try {
			// Check if there's already an active standalone SSE stream for this session
			if (streamMapping.containsKey(STANDALONE_SSE_STREAM_ID)) {
			  // Only one GET SSE stream is allowed per session
				StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_CONFLICT, StreamableHTTPServerServlet.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Conflict: Only one SSE stream is allowed per session"));
				return ;
			}
			
			// Assign the response to the standalone SSE stream
			streamMapping.put(STANDALONE_SSE_STREAM_ID, response);
			
			createAsyncContext(request, response)
			.addListener(new AsyncListener() {
				
				@Override
				public void onTimeout(AsyncEvent event) throws IOException {
					// TODO Auto-generated method stub
					Throwable ex = event.getThrowable();
					if (ex != null) {
						LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onTimeout() received from async event.", ex);
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
						LOGGER.log(Level.WARNING, "[sessionId="+getSessionId()+"] onError() received from async event.", ex);
						getMessageHandler().onError(ex);
					}
				}
				
				@Override
				public void onComplete(AsyncEvent event) throws IOException {
					// TODO Auto-generated method stub
					streamMapping.remove(STANDALONE_SSE_STREAM_ID);
				}
			});
			
//			StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_OK, StreamableHTTPServerServlet.TEXT_PLAIN, "OK");
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			getMessageHandler().onError(e);
		}
	}
	
	public void handleHttpPostRequest(final HttpServletRequest request, final HttpServletResponse response) {
		try {
			String contentBody = IOUtils.toString(request.getReader());
			JSONRPCMessage[] messages = MCPSchema.deserializeJSONRPCMessages(contentBody);
			boolean isInitializationRequest = Arrays.stream(messages).anyMatch(message -> (message instanceof JSONRPCRequest && ((JSONRPCRequest)message).getMethod().equals(InitializeRequest.METHOD_INITIALIZE)));
			
			// If an Mcp-Session-Id is returned by the server during initialization,
			// clients using the Streamable HTTP transport MUST include it 
			// in the Mcp-Session-Id header on all of their subsequent HTTP requests.
			String mcpSessionId = getSessionId();
			if (!isInitializationRequest && Strings.isNullOrEmpty(mcpSessionId)) {
				return ;
			}
			
			if (isInitializationRequest) {
				if (initialized.get() && !Strings.isNullOrEmpty(mcpSessionId)) {
					//Server was already initalised....
					StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, StreamableHTTPServerServlet.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Invalid Request: Server already initialized."));
					return ;
				}
				
				if (messages.length > 1) {
					StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, StreamableHTTPServerServlet.createJSONRPCError(ErrorCodes.CONNECTION_CLOSED, "Invalid Request: Only one initialization request is allowed."));
					return ;
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
				
				Optional<String> sessionIdOptional = StreamableHTTPServerServlet.validateSession(request, response);
				if (sessionIdOptional.isEmpty()) return ;
				
				if (!StreamableHTTPServerServlet.validateProtocolVersion(request, response)) return ;
			}
			
			boolean hasRequests = Arrays.stream(messages).anyMatch(message -> message instanceof JSONRPCRequest);
			if (!hasRequests) {
//				StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_ACCEPTED, StreamableHTTPServerServlet.TEXT_PLAIN, "Accepted");
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
				
				Arrays.stream(messages).forEach(message -> getMessageHandler().onMessage(message));
			} else {
				StreamId streamId = StreamId.of(UUID.randomUUID().toString());
				if (!enableJsonResponse) {
					createAsyncContext(request, response)
					.addListener(new AsyncListener() {
						
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
							streamMapping.remove(streamId);
						}
					});
					
//					StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_OK, StreamableHTTPServerServlet.TEXT_PLAIN, "OK");
					response.setStatus(HttpServletResponse.SC_OK);
				}
				
				Arrays.stream(messages).forEach(message -> {
					if (message instanceof JSONRPCRequest jsonRPCRequest) {
						streamMapping.put(streamId, response);
						requestToStreamMapping.put(jsonRPCRequest.getId(), streamId);
					}
					
					getMessageHandler().onMessage(message);
				});
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, StreamableHTTPServerServlet.createJSONRPCError(ErrorCodes.PARSE_ERROR, "Parse error.", e.getLocalizedMessage()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
//				e1.printStackTrace();
			}
			getMessageHandler().onError(e);
		}
	}
	
	private void replayEvents(final EventId lastEventId, final HttpServletRequest request, final HttpServletResponse response) {
		if (eventStore == null) return ;
		
		Sender sender = (eventId, message) -> {
//			return CompletableFuture.runAsync(() -> {
//				try {
//					writeSSEEvent(response, message, eventId);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					getMessageHandler().onError(new TransportException("Failed replay events", e));
//				}
//			});
			try {
				writeSSEEvent(response, message, eventId);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				getMessageHandler().onError(new TransportException("Failed replay events", e));
			}
		};
		
		createAsyncContext(request, response)
		.addListener(new AsyncListener() {
			
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
		
//		StreamableHTTPServerServlet.writeResponse(response, HttpServletResponse.SC_OK, StreamableHTTPServerServlet.TEXT_PLAIN, "OK");
		response.setStatus(HttpServletResponse.SC_OK);
		
		final StreamId streamId = eventStore.replayEventAfter(lastEventId, sender); //.get();
		streamMapping.put(streamId, response);
	}
	
	private AsyncContext createAsyncContext(final HttpServletRequest request, final HttpServletResponse response) {
		response.setContentType("text/event-stream");
		response.setCharacterEncoding(StreamableHTTPServerServlet.UTF_8);
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		if (!Strings.isNullOrEmpty(getSessionId())) {
			response.setHeader(StreamableHTTPServerServlet.MCP_SESSION_ID_HTTP_HEADER_NAME, getSessionId());
		}

		final AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);
		return asyncContext;
	}
}
