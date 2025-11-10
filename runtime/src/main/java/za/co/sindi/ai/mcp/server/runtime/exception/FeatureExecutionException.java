/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.exception;

/**
 * @author Buhake Sindi
 * @since 15 April 2025
 */
public class FeatureExecutionException extends RuntimeException {

	/**
	 * @param message
	 */
	public FeatureExecutionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public FeatureExecutionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FeatureExecutionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
