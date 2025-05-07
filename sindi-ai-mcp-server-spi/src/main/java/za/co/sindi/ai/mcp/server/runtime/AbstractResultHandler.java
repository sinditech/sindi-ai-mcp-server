/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.schema.JSONRPCRequest;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.Request;
import za.co.sindi.ai.mcp.schema.Result;
import za.co.sindi.ai.mcp.shared.RequestHandler;
import za.co.sindi.ai.mcp.shared.RequestHandlerExtra;

/**
 * @author Buhake Sindi
 * @since 22 April 2025
 */
public abstract class AbstractResultHandler<REQ extends Request, RES extends Result> implements RequestHandler<RES> {
	
	private final FeatureExecutor<REQ> executor;

	/**
	 * @param executor
	 */
	protected AbstractResultHandler(FeatureExecutor<REQ> executor) {
		super();
		this.executor = executor;
	}

	@Override
	public RES handle(JSONRPCRequest request, RequestHandlerExtra extra) {
		// TODO Auto-generated method stub
		Object value = null;
		Throwable throwable = null;
		
		try {
			value = executor.invoke(MCPSchema.toRequest(request));
		} catch (Throwable e) {
			throwable = e;
		}
		return generateResult(value, throwable);
	}

	protected abstract RES generateResult(final Object value, Throwable throwable);
}
