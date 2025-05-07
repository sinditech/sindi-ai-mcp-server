/**
 * 
 */
package za.co.sindi.ai.mcp.server.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import za.co.sindi.ai.mcp.schema.CallToolRequest;
import za.co.sindi.ai.mcp.schema.CallToolResult;
import za.co.sindi.ai.mcp.schema.EmptyResult;
import za.co.sindi.ai.mcp.schema.ErrorCodes;
import za.co.sindi.ai.mcp.schema.GetPromptRequest;
import za.co.sindi.ai.mcp.schema.GetPromptResult;
import za.co.sindi.ai.mcp.schema.ListPromptsRequest;
import za.co.sindi.ai.mcp.schema.ListPromptsResult;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesRequest;
import za.co.sindi.ai.mcp.schema.ListResourceTemplatesResult;
import za.co.sindi.ai.mcp.schema.ListResourcesRequest;
import za.co.sindi.ai.mcp.schema.ListResourcesResult;
import za.co.sindi.ai.mcp.schema.ListRootsRequest;
import za.co.sindi.ai.mcp.schema.ListRootsResult;
import za.co.sindi.ai.mcp.schema.ListToolsRequest;
import za.co.sindi.ai.mcp.schema.ListToolsResult;
import za.co.sindi.ai.mcp.schema.LoggingLevel;
import za.co.sindi.ai.mcp.schema.LoggingMessageNotification;
import za.co.sindi.ai.mcp.schema.Prompt;
import za.co.sindi.ai.mcp.schema.ReadResourceRequest;
import za.co.sindi.ai.mcp.schema.ReadResourceResult;
import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.schema.Root;
import za.co.sindi.ai.mcp.schema.Schema;
import za.co.sindi.ai.mcp.schema.SetLevelRequest;
import za.co.sindi.ai.mcp.schema.Tool;
import za.co.sindi.ai.mcp.server.BaseServer;
import za.co.sindi.ai.mcp.server.DefaultServer;
import za.co.sindi.ai.mcp.server.PromptManager;
import za.co.sindi.ai.mcp.server.RegisteredPrompt;
import za.co.sindi.ai.mcp.server.RegisteredResource;
import za.co.sindi.ai.mcp.server.RegisteredResourceTemplate;
import za.co.sindi.ai.mcp.server.RegisteredTool;
import za.co.sindi.ai.mcp.server.ResourceManager;
import za.co.sindi.ai.mcp.server.RootsProvider;
import za.co.sindi.ai.mcp.server.Server;
import za.co.sindi.ai.mcp.server.ToolManager;
import za.co.sindi.ai.mcp.server.runtime.FeatureDefinitionManager;
import za.co.sindi.ai.mcp.shared.MCPError;
import za.co.sindi.ai.mcp.shared.RequestHandler;
import za.co.sindi.commons.utils.Preconditions;
import za.co.sindi.commons.utils.Strings;

/**
 * @author Buhake Sindi
 * @since 06 May 2025
 */
public class FeatureManager implements PromptManager, ResourceManager, RootsProvider, ToolManager {
	
	private static final Logger LOGGER = Logger.getLogger(FeatureManager.class.getName());
	
	private final ConcurrentHashMap<String, RegisteredTool> tools = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, RegisteredPrompt> prompts = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, RegisteredResource> resources = new ConcurrentHashMap<>();
	
	private final ConcurrentHashMap<String, RegisteredResourceTemplate> resourceTemplates = new ConcurrentHashMap<>();
	
	private BaseServer server;

	/**
	 * @param server
	 * @param featureDefinitionManager
	 */
	public FeatureManager(BaseServer server, final FeatureDefinitionManager featureDefinitionManager) {
		super();
		this.server = server;
		featureDefinitionManager.getPrompts().stream().forEach(prompt -> prompts.put(prompt.getName(), new RegisteredPrompt(prompt, featureDefinitionManager.getPromptResultHandler(prompt.getName()))));
		featureDefinitionManager.getResources().stream().forEach(resource -> resources.put(resource.getUri(), new RegisteredResource(resource, featureDefinitionManager.getResourceResultHandler(resource.getUri()))));
		featureDefinitionManager.getResourceTemplates().stream().forEach(resourceTemplate -> resourceTemplates.put(resourceTemplate.getUriTemplate(), new RegisteredResourceTemplate(resourceTemplate, featureDefinitionManager.getResourceTemplatesResultHandler(resourceTemplate.getUriTemplate()))));
		featureDefinitionManager.getTools().stream().forEach(tool -> tools.put(tool.getName(), new RegisteredTool(tool, featureDefinitionManager.getToolResultHandler(tool.getName()))));
	}

	private RequestHandler<EmptyResult> setLoggingLevelRequestHandler(final Server server) {
		
		return (request, extra) -> {
			if (server instanceof DefaultServer ds) 
				ds.setLoggingLevel(LoggingLevel.of(String.valueOf(request.getParams().get("level"))));
			return Schema.EMPTY_RESULT;
		};
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
	
	public void registerServer(final Server server, final FeatureDefinitionManager featureDefinitionManager) {
		if (server.getServerCapabilities().getLogging() != null) {
			server.addRequestHandler(SetLevelRequest.METHOD_LOGGING_SETLEVEL, setLoggingLevelRequestHandler(server));
			server.addNotificationHandler(LoggingMessageNotification.METHOD_NOTIFICATION_LOGGING_MESSAGE, notification -> {});
		}
		
		if (server.getServerCapabilities().getPrompts() != null) {
			server.addRequestHandler(ListPromptsRequest.METHOD_LIST_PROMPTS, listPromptsRequestHandler());
			server.addRequestHandler(GetPromptRequest.METHOD_PROMPTS_GET, getPromptRequestHandler());
		}
		
		if (server.getServerCapabilities().getResources() != null) {
			server.addRequestHandler(ListResourcesRequest.METHOD_LIST_RESOURCES, listResourcesRequestHandler());
			server.addRequestHandler(ReadResourceRequest.METHOD_READ_RESOURCE, readResourcesRequestHandler());
			server.addRequestHandler(ListResourceTemplatesRequest.METHOD_LIST_RESOURCE_TEMPLATES, listResourceTemplatesRequestHandler());
		}
		
		if (server.getServerCapabilities().getTools() != null) {
			server.addRequestHandler(ListToolsRequest.METHOD_LIST_TOOLS, listToolsRequestHandler());
			server.addRequestHandler(CallToolRequest.METHOD_TOOLS_CALL, callToolsRequestHandler());
		}
	}

	@Override
	public void addTool(Tool tool, RequestHandler<CallToolResult> handler) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(tool != null, "Tool must not be null.");
		Preconditions.checkArgument(handler != null, "Tool call handler must not be null.");
		Preconditions.checkState(server.getServerCapabilities().getTools() != null, "Server does not support tools capability.");
		
		if (server.getServerCapabilities().getTools() != null) {
			LOGGER.info("Registering tool: " + tool.getName());
			tools.put(tool.getName(), new RegisteredTool(tool, handler));
			
			if (server.getServerCapabilities().getTools().getListChanged()) 
				server.sendToolListChanged();
		}
	}

	@Override
	public void removeTool(String toolName) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(toolName), "Tool name must not be null or empty.");
		Preconditions.checkState(server.getServerCapabilities().getTools() != null, "Server does not support tools capability.");
		
		if (server.getServerCapabilities().getTools() != null) {
			RegisteredTool removed = tools.remove(toolName);
			if (removed != null) {
				LOGGER.info("Removed tool: " + toolName);
				
				if (server.getServerCapabilities().getTools().getListChanged()) 
					server.sendToolListChanged();
			}
		}
	}

	@Override
	public Root[] listRoots(Server server) {
		// TODO Auto-generated method stub
		if (server instanceof DefaultServer ds && ds.getClientCapabilities().getRoots() == null) {
			throw new IllegalStateException("Client does not support listing roots. (required for " + ListRootsRequest.METHOD_ROOTS_LIST + ")");
		}
		
		return server.sendRequest(new ListRootsRequest(), ListRootsResult.class).thenApply(result -> result.getRoots()).join();
	}

	@Override
	public void addResource(Resource resource, RequestHandler<ReadResourceResult> readHandler) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(resource != null, "Resource must not be null.");
		Preconditions.checkArgument(readHandler != null, "Resource read handler must not be null.");
		Preconditions.checkState(server.getServerCapabilities().getResources() != null, "Server does not support resources capability.");
		
		if (server.getServerCapabilities().getResources() != null) {
			LOGGER.info("Registering Resource: " + resource.getUri());
			resources.put(resource.getUri(), new RegisteredResource(resource, readHandler));
			
			if (server.getServerCapabilities().getResources().getListChanged()) 
				server.sendResourceListChanged().join();
		}
	}

	@Override
	public void removeResource(String uri) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(uri), "Resource uri must not be null or empty.");
		Preconditions.checkState(server.getServerCapabilities().getResources() != null, "Server does not support resources capability.");
		
		if (server.getServerCapabilities().getResources() != null) {
			RegisteredResource removed = resources.remove(uri);
			if (removed != null) {
				LOGGER.info("Removed resource: " + uri);
				
				if (server.getServerCapabilities().getResources().getListChanged()) 
					server.sendResourceListChanged().join();
			}
		}
	}

	@Override
	public void addResourceTemplate(ResourceTemplate resourceTemplate, RequestHandler<ListResourceTemplatesResult> readCallback) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(resourceTemplate != null, "Resource template must not be null.");
		Preconditions.checkArgument(readCallback != null, "Resource read callback handler must not be null.");
		Preconditions.checkState(server.getServerCapabilities().getResources() != null, "Server does not support resources capability.");
		
		if (server.getServerCapabilities().getResources() != null) {
			LOGGER.info("Registering Resource template: " + resourceTemplate.getUriTemplate());
			resourceTemplates.put(resourceTemplate.getUriTemplate(), new RegisteredResourceTemplate(resourceTemplate, readCallback));
			
			if (server.getServerCapabilities().getResources().getListChanged()) 
				server.sendResourceListChanged().join();
		}
	}

	@Override
	public void removeResourceTemplate(String uriTemplate) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(uriTemplate), "Resource template uri must not be null or empty.");
		Preconditions.checkState(server.getServerCapabilities().getResources() != null, "Server does not support resources capability.");
		
		if (server.getServerCapabilities().getResources() != null) {
			RegisteredResourceTemplate removed = resourceTemplates.remove(uriTemplate);
			if (removed != null) {
				LOGGER.info("Removed resource template: " + uriTemplate);
				
				if (server.getServerCapabilities().getResources().getListChanged()) 
					server.sendResourceListChanged().join();
			}
		}
	}

	@Override
	public void addPrompt(Prompt prompt, RequestHandler<GetPromptResult> promptProvider) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(prompt != null, "Prompt must not be null.");
		Preconditions.checkArgument(promptProvider != null, "Prompt handler must not be null.");
		Preconditions.checkState(server.getServerCapabilities().getPrompts() != null, "Server does not support prompts capability.");
		
		if (server.getServerCapabilities().getPrompts() != null) {
			LOGGER.info("Registering Prompt: " + prompt.getName());
			prompts.put(prompt.getName(), new RegisteredPrompt(prompt, promptProvider));
			
			if (server.getServerCapabilities().getPrompts().getListChanged()) 
				server.sendPromptListChanged().join();
		}
	}

	@Override
	public void removePrompt(String promptName) {
		// TODO Auto-generated method stub
		Preconditions.checkArgument(!Strings.isNullOrEmpty(promptName), "Prompt name must not be null or empty.");
		Preconditions.checkState(server.getServerCapabilities().getPrompts() != null, "Server does not support prompts capability.");
		
		if (server.getServerCapabilities().getPrompts() != null) {
			RegisteredPrompt removed = prompts.remove(promptName);
			if (removed != null) {
				LOGGER.info("Removed prompt: " + promptName);
				
				if (server.getServerCapabilities().getPrompts().getListChanged()) 
					server.sendPromptListChanged().join();
			}
		}
	}
}
