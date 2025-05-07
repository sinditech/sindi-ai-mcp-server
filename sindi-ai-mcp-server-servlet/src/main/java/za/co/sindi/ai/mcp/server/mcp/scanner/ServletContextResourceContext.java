/**
 * 
 */
package za.co.sindi.ai.mcp.server.mcp.scanner;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import jakarta.servlet.ServletContext;
import za.co.sindi.resource.context.ResourceContext;

/**
 * @author Buhake Sindi
 * @since 04 April 2025
 */
public class ServletContextResourceContext implements ResourceContext {

	private ServletContext servletContext;
	
	/**
	 * @param servletContext
	 */
	public ServletContextResourceContext(ServletContext servletContext) {
		super();
		this.servletContext = servletContext;
	}

	@Override
	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return servletContext.getRealPath(path);
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		// TODO Auto-generated method stub
		return servletContext.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		// TODO Auto-generated method stub
		return servletContext.getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		// TODO Auto-generated method stub
		return servletContext.getResourcePaths(path);
	}
}
