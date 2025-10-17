package za.co.sindi.ai.mcp.server.rest;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import za.co.sindi.ai.mcp.mapper.JsonCursorSerialization;
import za.co.sindi.ai.mcp.mapper.JsonIncludeContextAdapter;
import za.co.sindi.ai.mcp.mapper.JsonJSONRPCVersionAdapter;
import za.co.sindi.ai.mcp.mapper.JsonLoggingLevelAdapter;
import za.co.sindi.ai.mcp.mapper.JsonProgressTokenSerialization;
import za.co.sindi.ai.mcp.mapper.JsonProtocolVersionAdapter;
import za.co.sindi.ai.mcp.mapper.JsonRequestIdSerialization;
import za.co.sindi.ai.mcp.mapper.JsonRoleAdapter;
import za.co.sindi.ai.mcp.mapper.JsonStringSchemaFormatAdapter;

/**
 * @author Buhake Sindi
 * @since 25 March 2025
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JsonbConfigProvider implements ContextResolver<Jsonb> {

	private final Jsonb jsonb;
	
	/**
	 * 
	 */
	public JsonbConfigProvider() {
		super();
		//TODO Auto-generated constructor stub
		JsonbConfig config = new JsonbConfig()
									.withAdapters(new JsonIncludeContextAdapter(),
											new JsonJSONRPCVersionAdapter(),
											new JsonLoggingLevelAdapter(),
											new JsonRoleAdapter(),
											new JsonProtocolVersionAdapter(),
											new JsonStringSchemaFormatAdapter())
								  .withSerializers(new JsonCursorSerialization(), new JsonRequestIdSerialization(), new JsonProgressTokenSerialization())
								  .withDeserializers(new JsonCursorSerialization(), new JsonRequestIdSerialization(), new JsonProgressTokenSerialization());
		this.jsonb = JsonbBuilder.create(config);
	}

	/* (non-Javadoc)
	 * @see jakarta.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
	 */
	@Override
	public Jsonb getContext(Class<?> type) {
		// TODO Auto-generated method stub
		return jsonb;
	}
}
