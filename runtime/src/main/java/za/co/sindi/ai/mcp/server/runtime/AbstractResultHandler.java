/**
 * 
 */
package za.co.sindi.ai.mcp.server.runtime;

import za.co.sindi.ai.mcp.schema.JSONRPCRequest;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.ai.mcp.schema.Request;
import za.co.sindi.ai.mcp.schema.Result;
import za.co.sindi.ai.mcp.server.exception.MCPException;
import za.co.sindi.ai.mcp.server.runtime.exception.FeatureExecutionException;
import za.co.sindi.ai.mcp.server.spi.MCPContext;
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
			registerRequest(extra);
			value = executor.invoke(MCPSchema.toRequest(request));
		} catch (Throwable e) {
			if (e instanceof FeatureExecutionException fee)	throw fee;
			if (!(e instanceof MCPException)) throw new FeatureExecutionException(e);
			
			throwable = e;
		}
		
		try {
			return generateResult(value, throwable);
		} finally {
			releaseRequest(extra);
		}
	}
	
	private void registerRequest(final RequestHandlerExtra extra) {
		MCPContext.getCurrentInstance().setCurrentRequest(extra.getRequestId(), extra.getMeta());
	}
	
	private void releaseRequest(final RequestHandlerExtra extra) {
		MCPContext.getCurrentInstance().setCurrentRequest(null, null);
	}

	protected abstract RES generateResult(final Object value, Throwable throwable);
}
