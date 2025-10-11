/**
 * 
 */
package za.co.sindi.ai.mcp.server.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Buhake Sindi
 * @since 06 October 2025
 */
@WebFilter(urlPatterns = "/*", asyncSupported=true)
public class CORSFilter implements Filter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Allow all origins (use with caution in production)
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        // Allowed HTTP methods
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE");
        
        // Exposed headers that the client can access
        response.setHeader("Access-Control-Expose-Headers", 
            "mcp-session-id, last-event-id, mcp-protocol-version");
        
        // Allowed request headers
        response.setHeader("Access-Control-Allow-Headers", 
            "Origin, Accept, Content-Type, Authorization, X-Requested-With");
        
        // Preflight cache duration (in seconds)
        response.setHeader("Access-Control-Max-Age", "1209600");
        
        // Handle OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        filterChain.doFilter(request, response);
	}
}
