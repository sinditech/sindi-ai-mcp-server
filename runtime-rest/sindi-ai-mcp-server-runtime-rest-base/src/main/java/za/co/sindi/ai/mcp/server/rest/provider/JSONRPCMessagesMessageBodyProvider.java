package za.co.sindi.ai.mcp.server.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import za.co.sindi.ai.mcp.schema.JSONRPCMessage;
import za.co.sindi.ai.mcp.schema.MCPSchema;
import za.co.sindi.commons.utils.IOUtils;

/**
 * @author Buhake Sindi
 * @since 26 March 2025
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JSONRPCMessagesMessageBodyProvider implements MessageBodyReader<JSONRPCMessage[]> {

	/* (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		// TODO Auto-generated method stub
		return JSONRPCMessage[].class.isAssignableFrom(type);
	}

	/* (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap, java.io.InputStream)
	 */
	@Override
	public JSONRPCMessage[] readFrom(Class<JSONRPCMessage[]> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		// TODO Auto-generated method stub
		return MCPSchema.deserializeJSONRPCMessages(IOUtils.toString(entityStream));
	}
}
