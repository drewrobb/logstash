package org.apache.mesos.logstash.executor.frameworks;

import org.apache.mesos.logstash.common.LogstashProtos.LogstashConfig;
import org.apache.mesos.logstash.executor.docker.DockerLogPath;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Docker configuration for the framework.
 */
public class DockerFramework implements Framework {

    private final ContainerId containerId;
    private final LogstashConfig frameworkInfo;
    private List<String> logLocations;

    public DockerFramework(LogstashConfig frameworkInfo, ContainerId containerId) {
        this.frameworkInfo = frameworkInfo;

        this.containerId = containerId;
        this.logLocations = parseLogLocations(frameworkInfo.getConfig());
    }

    public String getContainerId() {
        return this.containerId.id;
    }

    /**
     * Produces a valid logstash configuration from a (very similar looking) Framework configuration.
     */
    @Override
    public String getConfiguration() {
        String generatedConfiguration = frameworkInfo.getConfig();

        // Replace all log paths with paths to temporary files
        for (String logLocation : logLocations) {
            String localLocation = new DockerLogPath(this.containerId.id, frameworkInfo.getFrameworkName(),
                logLocation).getExecutorLogPath();
            generatedConfiguration = generatedConfiguration.replace(logLocation, localLocation);
        }

        // replace 'magic' string docker-path with normal path string
        return "# " + getName() + "\n" + generatedConfiguration.replace("docker-path", "path");
    }

    private List<String> parseLogLocations(String configuration) {
        List<String> locations = new ArrayList<>();
        Pattern pattern = Pattern.compile("docker-path\\s*=>\\s*\"([^}\\s]+)\"");

        Matcher matcher = pattern.matcher(configuration);

        while (matcher.find()) {
            locations.add(matcher.group(1));
        }

        return locations;
    }

    /**
     * Id of a container.
     */
    public static class ContainerId {
        String id;

        public ContainerId(String containerId) {
            this.id = containerId;
        }
    }

    public List<DockerLogPath> getLogFiles() {
        return logLocations.stream()
            .map((path) -> new DockerLogPath(this.containerId.id, this.getName(), path))
            .collect(Collectors.toList());
    }

    public String getName() {
        return frameworkInfo.getFrameworkName();
    }
}
