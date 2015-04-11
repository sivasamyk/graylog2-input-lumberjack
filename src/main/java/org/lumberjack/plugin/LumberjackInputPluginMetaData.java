package org.lumberjack.plugin;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class LumberjackInputPluginMetaData implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return "org.lumberjack.plugin.LumberjackInputPlugin";
    }

    @Override
    public String getName() {
        return "Logstash Lumberjack Input";
    }

    @Override
    public String getAuthor() {
        return "Sivasamy Kaliappan";
    }

    @Override
    public URI getURL() {
        // TODO Insert correct plugin website
        return URI.create("https://www.graylog.org/");
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Graylog input plugin to parse logstash-forwarder lumberjack protocol message.";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(1, 0, 0);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
