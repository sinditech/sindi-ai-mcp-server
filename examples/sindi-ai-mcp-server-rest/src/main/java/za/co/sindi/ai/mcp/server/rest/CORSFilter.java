/**
 * 
 */
package za.co.sindi.ai.mcp.server.rest;

import java.io.IOException;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * @author Buhake Sindi
 * @since 06 October 2025
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		// TODO Auto-generated method stub
		// Allow all origins (use with caution in production)
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        
        // Allowed HTTP methods
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE");
        
        // Exposed headers that the client can access
        responseContext.getHeaders().add("Access-Control-Expose-Headers", 
            "mcp-session-id, last-event-id, mcp-protocol-version");
        
        // Allow credentials (optional, often used with specific origins)
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        
        // Allowed request headers
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
            "Origin, Accept, Content-Type, Authorization, X-Requested-With");
        
        // Preflight cache duration (in seconds)
        responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
        
        // Handle OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(Response.Status.NO_CONTENT.getStatusCode());
        }
	}
}
