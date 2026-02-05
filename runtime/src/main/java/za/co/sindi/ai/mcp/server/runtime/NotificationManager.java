/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.shared.NotificationHandler;
import za.co.sindi.ai.mcp.shared.ProgressHandler;

/**
 * @author Buhake Sindi
 * @since 28 January 2026
 */
public interface NotificationManager {

	public NotificationHandler getCancellationNotification();
	public ProgressHandler getProgressNotification();
}
