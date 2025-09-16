/**
 * 
 */
package za.co.sindi.ai.mcp.server.impl;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.CallToolResult;
import za.co.sindi.ai.mcp.schema.ErrorCodes;
import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.schema.ListPromptsRequest;
import za.co.sindi.ai.mcp.schema.ListPromptsResult;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesResult;
import za.co.sindi.ai.mcp.schema.ListResourcesRequest;
import za.co.sindi.ai.mcp.schema.ListResourcesResult;
import za.co.sindi.ai.mcp.schema.ListToolsRequest;
import za.co.sindi.ai.mcp.schema.ListToolsResult;
import za.co.sindi.ai.mcp.schema.LoggingMessageNotification;
import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.schema.ReadResourceRequest;
import za.co.sindi.ai.mcp.schema.ReadResourceResult;
import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.schema.ServerCapabilities;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.server.MCPServerSession;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.RegisteredPrompt;
import za.co.sindi.ai.mcp.server.RegisteredResource;
import za.co.sindi.ai.mcp.server.RegisteredResourceTemplate;
import za.co.sindi.ai.mcp.server.RegisteredTool;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.server.runtime.SessionManager;
import za.co.sindi.ai.mcp.shared.MCPError;
import za.co.sindi.ai.mcp.shared.RequestHandler;
import za.co.sindi.commons.utils.Preconditions;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 06 May 2025
 */
public class MCPServerFeatureManager implements PromptManager, ResourceManager, ToolManager {
	
	private static final Logger LOGGER = Logger.getLogger(MCPServerFeatureManager.class.getName());
	
	private final ConcurrentHashMap<String, RegisteredTool> tools = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, RegisteredPrompt> prompts = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, RegisteredResource> resources = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, RegisteredResourceTemplate> resourceTemplates = new ConcurrentHashMap<>();
	
	private final ServerCapabilities capabilities;
	
	private final SessionManager sessionManager;
	
	/**
	 * @param capabilities
	 * @param sessionManager
	 * @param featureDefinitionManager
	 */
	public MCPServerFeatureManager(final ServerCapabilities capabilities, final SessionManager sessionManager, final FeatureDefinitionManager featureDefinitionManager) {
		super();
		this.capabilities = Objects.requireNonNull(capabilities, "A MCP server capabilities is required.");
		this.sessionManager = Objects.requireNonNull(sessionManager, "A MCP session manager required.");
		featureDefinitionManager.getPrompts().stream().forEach(prompt -> prompts.put(prompt.getName(), new RegisteredPrompt(prompt, featureDefinitionManager.getPromptResultHandler(prompt.getName()))));
		featureDefinitionManager.getResources().stream().forEach(resource -> resources.put(resource.getUri(), new RegisteredResource(resource, featureDefinitionManager.getResourceResultHandler(resource.getUri()))));
		featureDefinitionManager.getResourceTemplates().stream().forEach(resourceTemplate -> resourceTemplates.put(resourceTemplate.getUriTemplate(), new RegisteredResourceTemplate(resourceTemplate, featureDefinitionManager.getResourceTemplatesResultHandler(resourceTemplate.getUriTemplate()))));
		featureDefinitionManager.getTools().stream().forEach(tool -> tools.put(tool.getName(), new RegisteredTool(tool, featureDefinitionManager.getToolResultHandler(tool.getName()))));
	}
	
	private RequestHandler<ListPromptsResult> listPromptsRequestHandler() {
		
		return (request, extra) -> {
			var promptList = prompts.values().stream().map(prompt -> prompt.getPrompt()).toList();
			ListPromptsResult result = new ListPromptsResult();
			result.setPrompts(promptList.toArray(new Prompt[promptList.size()]));
			return result;
		};
	}
	
	private RequestHandler<GetPromptResult> getPromptRequestHandler() {
		
		return (request, extra) -> {
			String promptName = String.valueOf(request.getParams().get("name"));
			RegisteredPrompt registeredPrompt = prompts.get(promptName);
			if (registeredPrompt == null) {
				throw new MCPError(ErrorCodes.INVALID_PARAMS ,"Prompt not found: " + promptName);
			}
			
			return registeredPrompt.getMessageProvider().handle(request, extra);
		};
	}
	
	private RequestHandler<ListResourcesResult> listResourcesRequestHandler() {
		
		return (request, extra) -> {
			var resourceList = resources.values().stream().map(resource -> resource.getResource()).toList();
			ListResourcesResult result = new ListResourcesResult();
			result.setResources(resourceList.toArray(new Resource[resourceList.size()]));
			return result;
		};
	}
	
	private RequestHandler<ReadResourceResult> readResourcesRequestHandler() {
		
		return (request, extra) -> {
			String resourceUri = String.valueOf(request.getParams().get("uri"));
			RegisteredResource registeredResource = resources.get(resourceUri);
			if (registeredResource == null) {
				throw new MCPError(ErrorCodes.INVALID_PARAMS ,"Resource with uri '" + resourceUri + "' not found.");
			}
			
			return registeredResource.getReadHandler().handle(request, extra);
		};
	}
	
	private RequestHandler<ListResourceTemplatesResult> listResourceTemplatesRequestHandler() {
		return (request, extra) -> {
			var resourceTemplateList = resourceTemplates.values().stream().map(resourceTemplate -> resourceTemplate.getResourceTemplate()).toList();
			ListResourceTemplatesResult result = new ListResourceTemplatesResult();
			result.setResourceTemplates(resourceTemplateList.toArray(new ResourceTemplate[resourceTemplateList.size()]));
			return result;
		};
	}
	
	private RequestHandler<ListToolsResult> listToolsRequestHandler() {
		
		return (request, extra) -> {
			var toolsList = tools.values().stream().map(tool -> tool.getTool()).toList();
			ListToolsResult result = new ListToolsResult();
			result.setTools(toolsList.toArray(new Tool[toolsList.size()]));
			return result;
		};
		
	}
	
	private RequestHandler<CallToolResult> callToolsRequestHandler() {
		return (request, extra) -> {
			String toolName = String.valueOf(request.getParams().get("name"));
			RegisteredTool registeredTool = tools.get(toolName);
			if (registeredTool == null) {
				throw new MCPError(ErrorCodes.INVALID_PARAMS ,"Tool not found: " + toolName);
			}
			
			return registeredTool.getHandler().handle(request, extra);
		};
	}
	
	public void setup(final MCPServerSession serverSession) {
		if (serverSession.getCapabilities().getLogging() != null) {
//			server.addRequestHandler(SetLevelRequest.METHOD_LOGGING_SETLEVEL, setLoggingLevelRequestHandler(server));
			serverSession.addNotificationHandler(LoggingMessageNotification.METHOD_NOTIFICATION_LOGGING_MESSAGE, notification -> {});
		}
		
		if (serverSession.getCapabilities().getPrompts() != null) {
			serverSession.addRequestHandler(ListPromptsRequest.METHOD_LIST_PROMPTS, listPromptsRequestHandler());
			serverSession.addRequestHandler(GetPromptRequest.METHOD_PROMPTS_GET, getPromptRequestHandler());
		}
		
		if (serverSession.getCapabilities().getResources() != null) {
			serverSession.addRequestHandler(ListResourcesRequest.METHOD_LIST_RESOURCES, listResourcesRequestHandler());
			serverSession.addRequestHandler(ReadResourceRequest.METHOD_READ_RESOURCE, readResourcesRequestHandler());
			serverSession.addRequestHandler(ListResourceTemplatesRequest.METHOD_LIST_RESOURCE_TEMPLATES, listResourceTemplatesRequestHandler());
		}
		
		if (serverSession.getCapabilities().getTools() != null) {
			serverSession.addRequestHandler(ListToolsRequest.METHOD_LIST_TOOLS, listToolsRequestHandler());
			serverSession.addRequestHandler(CallToolRequest.METHOD_TOOLS_CALL, callToolsRequestHandler());
		}
	}

	@Override
	public void addTool(Tool tool, RequestHandler<CallToolResult> handler) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(tool != null, "Tool must not be null.");
		Preconditions.checkArgument(handler != null, "Tool call handler must not be null.");
		Preconditions.checkState(capabilities.getTools() != null, "Server does not support tools capability.");
		
		if (capabilities.getTools() != null) {
			LOGGER.info("Registering tool: " + tool.getName());
			tools.put(tool.getName(), new RegisteredTool(tool, handler));
			
			if (capabilities.getTools().getListChanged()) 
//				server.sendToolListChanged();
				notifyToolsListChanged().join();
		}
	}

	@Override
	public void removeTool(String toolName) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(toolName), "Tool name must not be null or empty.");
		Preconditions.checkState(capabilities.getTools() != null, "Server does not support tools capability.");
		
		if (capabilities.getTools() != null) {
			RegisteredTool removed = tools.remove(toolName);
			if (removed != null) {
				LOGGER.info("Removed tool: " + toolName);
				
				if (capabilities.getTools().getListChanged()) 
//					server.sendToolListChanged();
					notifyToolsListChanged().join();
			}
		}
	}

	@Override
	public void addResource(Resource resource, RequestHandler<ReadResourceResult> readHandler) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(resource != null, "Resource must not be null.");
		Preconditions.checkArgument(readHandler != null, "Resource read handler must not be null.");
		Preconditions.checkState(capabilities.getResources() != null, "Server does not support resources capability.");
		
		if (capabilities.getResources() != null) {
			LOGGER.info("Registering Resource: " + resource.getUri());
			resources.put(resource.getUri(), new RegisteredResource(resource, readHandler));
			
			if (capabilities.getResources().getListChanged()) 
//				server.sendResourceListChanged().join();
				notifyResourcesListChanged().join();
		}
	}

	@Override
	public void removeResource(String uri) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(uri), "Resource uri must not be null or empty.");
		Preconditions.checkState(capabilities.getResources() != null, "Server does not support resources capability.");
		
		if (capabilities.getResources() != null) {
			RegisteredResource removed = resources.remove(uri);
			if (removed != null) {
				LOGGER.info("Removed resource: " + uri);
				
				if (capabilities.getResources().getListChanged()) 
//					server.sendResourceListChanged().join();
					notifyResourcesListChanged().join();
			}
		}
	}

	@Override
	public void addResourceTemplate(ResourceTemplate resourceTemplate, RequestHandler<ListResourceTemplatesResult> readCallback) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(resourceTemplate != null, "Resource template must not be null.");
		Preconditions.checkArgument(readCallback != null, "Resource read callback handler must not be null.");
		Preconditions.checkState(capabilities.getResources() != null, "Server does not support resources capability.");
		
		if (capabilities.getResources() != null) {
			LOGGER.info("Registering Resource template: " + resourceTemplate.getUriTemplate());
			resourceTemplates.put(resourceTemplate.getUriTemplate(), new RegisteredResourceTemplate(resourceTemplate, readCallback));
			
			if (capabilities.getResources().getListChanged()) 
//				server.sendResourceListChanged().join();
				notifyResourcesListChanged().join();
		}
	}

	@Override
	public void removeResourceTemplate(String uriTemplate) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(uriTemplate), "Resource template uri must not be null or empty.");
		Preconditions.checkState(capabilities.getResources() != null, "Server does not support resources capability.");
		
		if (capabilities.getResources() != null) {
			RegisteredResourceTemplate removed = resourceTemplates.remove(uriTemplate);
			if (removed != null) {
				LOGGER.info("Removed resource template: " + uriTemplate);
				
				if (capabilities.getResources().getListChanged()) 
//					server.sendResourceListChanged().join();
					notifyResourcesListChanged().join();
			}
		}
	}

	@Override
	public void addPrompt(Prompt prompt, RequestHandler<GetPromptResult> promptProvider) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(prompt != null, "Prompt must not be null.");
		Preconditions.checkArgument(promptProvider != null, "Prompt handler must not be null.");
		Preconditions.checkState(capabilities.getPrompts() != null, "Server does not support prompts capability.");
		
		if (capabilities.getPrompts() != null) {
			LOGGER.info("Registering Prompt: " + prompt.getName());
			prompts.put(prompt.getName(), new RegisteredPrompt(prompt, promptProvider));
			
			if (capabilities.getPrompts().getListChanged()) 
//				server.sendPromptListChanged().join();
				notifyPromptsListChanged().join();
		}
	}

	@Override
	public void removePrompt(String promptName) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(promptName), "Prompt name must not be null or empty.");
		Preconditions.checkState(capabilities.getPrompts() != null, "Server does not support prompts capability.");
		
		if (capabilities.getPrompts() != null) {
			RegisteredPrompt removed = prompts.remove(promptName);
			if (removed != null) {
				LOGGER.info("Removed prompt: " + promptName);
				
				if (capabilities.getPrompts().getListChanged()) 
//					server.sendPromptListChanged().join();
					notifyPromptsListChanged().join();
			}
		}
	}
	
	private CompletableFuture<Void> notifyToolsListChanged() {
		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] allFutures = new CompletableFuture[sessionManager.totalSessions()];
		int i = 0;
		for (Server server : sessionManager.getSessions()) {
			allFutures[i++] = server.sendToolListChanged();
		}
		return CompletableFuture.allOf(allFutures);
	}
	
	private CompletableFuture<Void> notifyResourcesListChanged() {
		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] allFutures = new CompletableFuture[sessionManager.totalSessions()];
		int i = 0;
		for (Server server : sessionManager.getSessions()) {
			allFutures[i++] = server.sendResourceListChanged();
		}
		return CompletableFuture.allOf(allFutures);
	}
	
	private CompletableFuture<Void> notifyPromptsListChanged() {
		@SuppressWarnings("unchecked")
		CompletableFuture<Void>[] allFutures = new CompletableFuture[sessionManager.totalSessions()];
		int i = 0;
		for (Server server : sessionManager.getSessions()) {
			allFutures[i++] = server.sendPromptListChanged();
		}
		return CompletableFuture.allOf(allFutures);
	}
}
