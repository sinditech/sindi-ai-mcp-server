 sindi-ai-mcp-server
Java Implementation of Anthropic's Model Context Protocol (MCP) Server.

This implementation is an attempt to provide an integration between the MCP protocol and Jakarta EE application in a seamless way.

## 🏗️ Architecture

The project is structured into several modules, each serving a specific purpose:

### Core Modules

- **`sindi-ai-mcp-server-spi`**: Fundamental server-side MCP classes, interfaces, annotations and SPI definitions 
- **`sindi-ai-mcp-server-runtime`**: Runtime implementation of the SPI for runtime server registration and service discovery.
- **`sindi-ai-mcp-server-features`**: Example MCP features showcasing the power of MCP, the Jakarta EE way.

With 2 runtimes implementations:
- **`runtime-rest`**: This module contains the implementations of RESTful MCP server-side transports with Jakarta REST.
  + **`sindi-ai-mcp-server-runtime-rest-base`**: The Jakarta REST base package used by all Jakarta REST implementations mentioned below.
  + **`sindi-ai-mcp-server-runtime-rest-sse`**: The Jakarta REST implementation of MCP SSE transport.
  + **`sindi-ai-mcp-server-runtime-rest-streamable-http`**: The Jakarta REST implementation of MCP Streamable HTTP transport. 

- **`runtime-servlet`**: This module contains the implementations of web-based MCP server-side transports with Jakarta Servlet.
  + **`sindi-ai-mcp-server-runtime-servlet-sse`**: The Jakarta Servlet implementation of MCP SSE transport.
  + **`sindi-ai-mcp-server-runtime-servlet-streamable-http`**: The Jakarta Servlet implementation of MCP Streamable HTTP transport. 

## 🚀 Quick Start

### 1. Add Dependencies

You need to either select the servlet or REST runtime, within your application (but not both, as there might be an endpoint conflicts). 

Add the required dependencies to your `pom.xml`.

- For Servlet:

```xml
<dependency>
	<groupId>za.co.sindi</groupId>
	<artifactId>sindi-ai-mcp-server-runtime-servlet</artifactId>
	<version>${project.version}</version>
</dependency>

```

- For REST:

```xml
<dependency>
	<groupId>za.co.sindi</groupId>
	<artifactId>sindi-ai-mcp-server-runtime-rest</artifactId>
	<version>${project.version}</version>
</dependency>

```

### 2. Define an MCP Feature Service

MCP Features can be registered either on simple POJO or CDI-managed bean.

The following MCP features are supported:

- **`Tools`**: supported annotations are `@Tool`, `@ToolArgument` on methods. LangChain4J tool annotations are also supported. The method result must be of type `String`.
- **`Prompts`**: supported annotations are `@Prompt`, `@PromptArgument` on methods. The method result must be of type `PromptMessage` or `PromptMessage[]`.
- **`Resource`**: supported annotation, `@Resource` (`uri` annotation attribute is mandatory) on methods. The method result must be of type `ResourceContents[]` (either of type `BlobResourceContents` or `TextResourceContents`).
- **`Resource template`**: supported annotation, `@ResourceTemplate` (`uri` annotation attribute is mandatory) on methods. The method result must be of type `ResourceTemplate[]`.

For more programmatic approach, you can register an MCP feature with its callback handler by injecting its appropriate manager to the MCP server runtime:

- **`ToolManager`**: For tool registration.
- **`PromptManager`**: For prompt registration.
- **`ResourceManager`**: For resource and resource template registration.

Additionally, you can inject the following MCP resources:

- **`MCPLogger`**: MCP Logging to client session (if enabled).
- **`MCPContext`**: MCP Context, providing manual MCP features and client session.


## 📖 Examples

The project includes comprehensive examples for MCP servers, found in the `examples/` directory. There are 2 examples, each demonstrating MCP on their various Jakarta EE server protocols (it bundles the `sindi-ai-mcp-server-features` module, exposing the MCP features on the respective Jakarta EE server protocols):

- **`sindi-ai-mcp-server-rest`**: Running MCP server (bundled with the MCP features) on REST protocol.
- **`sindi-ai-mcp-server-servlet`**: Running MCP server (bundled with the MCP features) on Servlet protocol.

## 🛠️ How to run examples

1. Git clone the entire project.
2. Build the project.

```
mvn clean package -e
```

3. `cd examples/sindi-ai-mcp-server-xxxx/`, where `xxxx` can be either `rest` or `servlet`.

4. Run the application as follows:

```
mvn clean liberty:dev -e
```

5. Open a new terminal and start MCP Inspector.

```
npx -y @modelcontextprotocol/inspector@latest
```

The browser should automatically open with MCP Inspector.

6. Copy the link and connect to your MCP server.

For SSE, the link should be `http://localhost:9080/sse`.
For Streamable HTTP, the link should be `http://localhost:9080/mcp`.

Click connect on the MCP inspector and start testing your MCP features.


## 🤝 Contributing

If you want to contribute, please have a look at [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🌟 Getting Started

Ready to integrate AI into your enterprise Java application? 

1. **Explore the examples**: Start with the MCP-specific examples in the `examples/` directory.
2. **Read the documentation**: Check out individual module documentation for detailed configuration options
3. **Contribute**: Help improve the project by reporting issues or submitting pull requests

