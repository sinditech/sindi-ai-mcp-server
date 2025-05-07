/**
 * 
 */
package za.co.sindi.ai.mcp.server;

import za.co.sindi.ai.mcp.schema.ListResourceTemplatesResult;
import za.co.sindi.ai.mcp.schema.ReadResourceResult;
import za.co.sindi.ai.mcp.schema.Resource;
import za.co.sindi.ai.mcp.schema.ResourceTemplate;
import za.co.sindi.ai.mcp.shared.RequestHandler;

/**
 * @author Buhake Sindi
 * @since 01 May 2025
 */
public interface ResourceManager {

	public void addResource(final Resource resource, final RequestHandler<ReadResourceResult> readHandler);
	public void removeResource(final String uri);
	
	public void addResourceTemplate(final ResourceTemplate resourceTemplate, final RequestHandler<ListResourceTemplatesResult> readCallback);
	public void removeResourceTemplate(final String uriTemplate);
}
