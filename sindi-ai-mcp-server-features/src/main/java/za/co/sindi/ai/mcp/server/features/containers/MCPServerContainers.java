///usr/bin/env jbang "$0" "$@" ; exit $?
package za.co.sindi.ai.mcp.server.features.containers;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.ListVolumesResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import za.co.sindi.ai.mcp.schema.PromptMessage;
import za.co.sindi.ai.mcp.schema.Role;
import za.co.sindi.ai.mcp.schema.TextContent;
import za.co.sindi.ai.mcp.server.exception.ToolCallException;
import za.co.sindi.ai.mcp.server.spi.Prompt;
import za.co.sindi.ai.mcp.server.spi.Tool;
import za.co.sindi.ai.mcp.server.spi.ToolArgument;

@ApplicationScoped
public class MCPServerContainers {
	
	private static final Logger LOGGER = Logger.getLogger(MCPServerContainers.class.getName());

    private DockerClientConfig config;
    private DockerClient dockerClient;

    @PostConstruct
    void init() {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        LOGGER.info("Starting Docker server with master URL: " + config.getDockerHost());
        var dockerHttpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(config, dockerHttpClient);

    }

    @Tool(description = "Get the current docker/container configuration")
    public String configuration_get() {
        return dockerClient.toString();
    }

    @Tool(description = "Get the current list of containers")
    public List<Container> containers_list() {
        return dockerClient.listContainersCmd().exec();
    }

    @Tool(description = "Get the current list of images of containers")
    public List<Image> images_list() {
        return dockerClient.listImagesCmd().exec();
    }

    @Tool(description = "Copies (pulls) a Docker or Podman container image from a registry onto the local machine storage")
    public String image_pull(
            @ToolArgument(description = "Docker or Podman container image name to pull", required = true) String imageName) {
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new ResultCallback.Adapter<PullResponseItem>())
                    .awaitCompletion();
            return "Image pulled: " + imageName;
        } catch (InterruptedException e) {
            throw new ToolCallException("Failed to pull image: " + imageName, e);
        }
    }

    @Tool(description = "Pushes a Docker or Podman container image, manifest list or image index from local machine storage to a registry")
    public String image_push(
            @ToolArgument(description = "Docker or Podman container image name to push", required = true) String imageName) {
        try {
            dockerClient.pushImageCmd(imageName)
                    .exec(new ResultCallback.Adapter<PushResponseItem>())
                    .awaitCompletion();
            return "Image pushed: " + imageName;
        } catch (InterruptedException e) {
            throw new ToolCallException("Failed to push image: " + imageName, e);
        }
    }

    @Tool(description = "Removes a Docker or Podman image from the local machine storage")
    public String image_remove(
            @ToolArgument(description = "Docker or Podman container image name to remove", required = true) String imageName) {
        dockerClient.removeImageCmd(imageName).exec();
        return "Image removed: " + imageName;
    }

    @Tool(description = "Get the current list of networks of containers")
    public List<Network> networks_list() {
        return dockerClient.listNetworksCmd().exec();
    }

    @Tool(description = "Get the current list of volumes for containers")
    public ListVolumesResponse volumes_list() {
        return dockerClient.listVolumesCmd().exec();
    }

    @Tool(description = "Get logs from container")
    public List<String> container_logs(@ToolArgument(description = "The name of the container") String name,
            @ToolArgument(description = "The number of lines to return") int lines) {
        var logContainerCmd = dockerClient.logContainerCmd(name).withStdOut(true)
                .withStdErr(true).withTail(lines);
        List<String> logs = new ArrayList<>();
        try {
            logContainerCmd.exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame object) {
                    logs.add(object.toString());
                }
            }).awaitCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return logs;
    }

    @Tool(description = "Get the low-level information and configuration of a Docker or Podman container with the specified container ID or name")
    InspectContainerResponse container_inspect(
            @ToolArgument(description = "Docker or Podman container ID or name to displays the information") String name) {
        return dockerClient.inspectContainerCmd(name).exec();
    }

    @Tool(description = "Removes a Docker or Podman container with the specified container ID or name (rm)")
    String container_remove(@ToolArgument(description = "Docker or Podman container ID or name to remove") String name) {
        dockerClient.removeContainerCmd(name).exec();
        return "Container removed: " + name;
    }

    @Tool(description = "Runs a Docker or Podman container with the specified image name")
    public String container_run(
            @ToolArgument(description = "Docker or Podman container image name to pull", required = true) String imageName,
            @ToolArgument(description = """
                    Port mappings to expose on the host. Format: <hostPort>:<containerPort>.
                    Example: 8080:80. (Optional, add only to expose ports)
                    """) List<String> ports,
            @ToolArgument(description = """
                    Environment variables to set in the container.
                    Format: <key>=<value>.
                    Example: FOO=bar.
                    (Optional, add only to set environment variables)
                    """) List<String> environment) {

        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(imageName);

        if (ports != null && !ports.isEmpty()) {
            List<PortBinding> portBindings = new ArrayList<>();
            for (String port : ports) {
                String[] parts = port.split(":");
                portBindings.add(PortBinding.parse(parts[0] + ":" + parts[1]));
            }
            containerCmd.withHostConfig(HostConfig.newHostConfig().withPortBindings(portBindings));
        }

        if (environment != null && !environment.isEmpty()) {
            containerCmd.withEnv(environment);
        }

        CreateContainerResponse container = containerCmd.exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        return "Container started: " + container.getId();
    }

    @Tool(description = "Stops a Docker or Podman running container with the specified container ID or name")
    String container_stop(@ToolArgument(description = "Docker or Podman container ID or name to stop") String name) {
        dockerClient.stopContainerCmd(name).exec();
        return "Container stopped: " + name;
    }

    @Tool(description = "Build a Docker or Podman image from a Dockerfile, Podmanfile, or Containerfile")
    String image_build(
            @ToolArgument(description = """
                        The absolute path to the Dockerfile, Podmanfile, or Containerfile
                    to build the image from""", required = true) String containerFile,
            @ToolArgument(description = """
                    Specifies the name which is assigned to the resulting image
                    if the build process completes successfully (--tag, -t)""") String imageName) {

        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd()
                .withDockerfile(new File(containerFile));

        if (imageName != null && !imageName.isEmpty()) {
            buildImageCmd.withTags(Collections.singleton(imageName));
        }

        String imageId = buildImageCmd.start().awaitImageId();
        return "Image built successfully: " + imageId;
    }

    @Prompt(description = "Service Architecture Diagram")
    PromptMessage service_architecture_diagram() {
    	PromptMessage userPromptMessage = new PromptMessage();
    	userPromptMessage.setRole(Role.USER);
    	userPromptMessage.setContent(new TextContent(
                """
                        Generate a service architecture diagram showing how my containers interconnect to form complete applications.
                        """));
    	
    	return userPromptMessage;
    }

    @Prompt(description = "Port Allocation Overview")
    PromptMessage port_allocation_overview() {
    	PromptMessage userPromptMessage = new PromptMessage();
    	userPromptMessage.setRole(Role.USER);
    	userPromptMessage.setContent(new TextContent(
                """
                        Create a visualization of all port mappings across containers, highlighting exposed ports and potential conflicts.
                        """));
    	return userPromptMessage;
    }
}
