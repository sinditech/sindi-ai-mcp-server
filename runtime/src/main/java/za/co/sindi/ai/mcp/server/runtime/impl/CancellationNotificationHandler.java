/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime.impl;

import za.co.sindi.ai.mcp.schema.CancelledNotification;
import za.co.sindi.ai.mcp.schema.JSONRPCNotification;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.RequestId;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
import za.co.sindi.ai.mcp.shared.NotificationHandler;

/**
 * @author Buhake Sindi
 * @since 01 February 2026
 */
public class CancellationNotificationHandler implements NotificationHandler {

	@Override
	public void handle(JSONRPCNotification notification) {
		// TODO Auto-generated method stub
		CancelledNotification cancelledNotification = MCPSchema.toNotification(notification);
		RequestId requestId = cancelledNotification.getParameters().getRequestId();
		if (requestId != null) {
			MCPContext.getCurrentInstance().cancelRequest(requestId, cancelledNotification.getParameters().getReason());
		}
	}
}
