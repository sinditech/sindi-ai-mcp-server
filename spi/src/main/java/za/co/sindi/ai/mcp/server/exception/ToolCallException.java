/**
 * 
 */
package za.co.sindi.ai.mcp.server.exception;

/**
 * @author Buhake Sindi
 * @since 15 April 2025
 */
public class ToolCallException extends MCPException {

	/**
	 * @param message
	 */
	public ToolCallException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ToolCallException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ToolCallException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
