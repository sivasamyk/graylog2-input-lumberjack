package org.lumberjack.plugin;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.NumberField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.inputs.MessageInput;
import org.graylog2.plugin.inputs.MisfireException;
import org.graylog2.plugin.inputs.annotations.ConfigClass;
import org.graylog2.plugin.inputs.annotations.FactoryClass;
import org.graylog2.plugin.inputs.codecs.CodecAggregator;
import org.graylog2.plugin.inputs.transports.Transport;
import org.graylog2.plugin.journal.RawMessage;
import org.lumberjack.server.LogEvent;
import org.lumberjack.server.LogEventListener;
import org.lumberjack.server.LumberjackServer;
import org.lumberjack.server.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 9/4/15.
 */
public class LumberjackTransport implements Transport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LumberjackTransport.class.getName());
    private final Configuration configuration;
    private final MetricRegistry metricRegistry;
    private ServerStatus serverStatus;
    private LumberjackServer lumberjackServer;
    private static final String CK_KEYSTORE_PATH = "keystorePath";
    private static final String CK_KEYSTORE_PASSWORD = "keystorePassword";
    private static final String CK_KEY_PASSWORD = "keyPassword";
    private static final String CK_BIND_IP = "bindIP";
    private static final String CK_BIND_PORT = "bindPort";

    @AssistedInject
    public LumberjackTransport(@Assisted Configuration configuration,
                               MetricRegistry metricRegistry,
                               ServerStatus serverStatus) {
        this.configuration = configuration;
        this.metricRegistry = metricRegistry;
        this.serverStatus = serverStatus;
    }

    @Override
    public void setMessageAggregator(CodecAggregator codecAggregator) {

    }

    @Override
    public void launch(final MessageInput messageInput) throws MisfireException {
        LogEventListener listener = new LogEventListener() {
            @Override
            public void onEvents(List<LogEvent> list) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (LogEvent event : list) {
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        mapper.writeValue(byteStream,convertToGELF(event));
                        messageInput.processRawMessage(new RawMessage(byteStream.toByteArray()));
                        byteStream.close();
                    }
                } catch (Exception e) {
                    LOGGER.warn("Exception while processing event ",e);
                }
            }
        };
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setIpAddress(configuration.getString(CK_BIND_IP));
        serverConfiguration.setPort(configuration.getInt(CK_BIND_PORT));
        serverConfiguration.setKeyStorePath(configuration.getString(CK_KEYSTORE_PATH));
        serverConfiguration.setKeyStorePass(configuration.getString(CK_KEYSTORE_PASSWORD));
        serverConfiguration.setKeyPass(configuration.getString(CK_KEY_PASSWORD));
        LOGGER.info("Starting LumberjackTransport with config :" + configuration);
        lumberjackServer = new LumberjackServer(serverConfiguration,
                listener);
        lumberjackServer.start();
        LOGGER.info("Lumberjack transport started");
    }

    private Map<String,String> convertToGELF(LogEvent lumberjackEvent) {
        Map<String,String> eventData = new HashMap<>(lumberjackEvent.getEventData().size() + 1);
        eventData.put("version", "1.1");
        eventData.put("host",lumberjackEvent.getEventData().remove("host"));
        eventData.put("file",lumberjackEvent.getEventData().remove("file"));
        eventData.put("short_message", lumberjackEvent.getEventData().remove("line"));
        for(Map.Entry<String,String> entry : lumberjackEvent.getEventData().entrySet())
        {
            eventData.put("_" + entry.getKey(),entry.getValue());
        }
        return eventData;
    }

    @Override
    public void stop() {
        lumberjackServer.stop();
    }

    @Override
    public MetricSet getMetricSet() {
        return null;
    }

    @FactoryClass
    public interface Factory extends Transport.Factory<LumberjackTransport> {
        @Override
        LumberjackTransport create(Configuration configuration);

        @Override
        Config getConfig();
    }

    @ConfigClass
    public static class Config implements Transport.Config {
        @Override
        public ConfigurationRequest getRequestedConfiguration() {
            final ConfigurationRequest cr = new ConfigurationRequest();
            cr.addField(new TextField(CK_KEYSTORE_PATH,
                    "Keystore Path",
                    "",
                    "Absolute path of JKS keystore"));
            cr.addField(new TextField(CK_KEYSTORE_PASSWORD,
                    "Keystore Password",
                    "",
                    "-deststorepass argument in keytool",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    TextField.Attribute.IS_PASSWORD));
            cr.addField(new TextField(CK_KEY_PASSWORD,
                    "Key Password",
                    "",
                    "-destkeypass argument in keytool",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    TextField.Attribute.IS_PASSWORD));
            cr.addField(new TextField(CK_BIND_IP,
                    "Bind IP Address",
                    "0.0.0.0",
                    "Local IP Address to bind",
                    ConfigurationField.Optional.NOT_OPTIONAL));
            cr.addField(new NumberField(CK_BIND_PORT,
                    "Port",
                    5043,
                    "Local port to listen for events",
                    ConfigurationField.Optional.NOT_OPTIONAL,
                    NumberField.Attribute.IS_PORT_NUMBER));
            return cr;
        }
    }
}
