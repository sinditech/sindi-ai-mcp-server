/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.exception;

/**
 * @author Buhake Sindi
 * @since 16 April 2025
 */
public class MCPException extends RuntimeException {

	/**
	 * @param message
	 */
	public MCPException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public MCPException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public MCPException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
