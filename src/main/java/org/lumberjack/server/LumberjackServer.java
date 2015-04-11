package org.lumberjack.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 7/4/15.
 */
public class LumberjackServer {
    private ServerBootstrap bootstrap;
    private LogEventListener eventListener;
    private ServerConfiguration configuration;

    public LumberjackServer(ServerConfiguration configuration,
                            LogEventListener eventListener) {
        this.configuration = configuration;
        this.eventListener = eventListener;
    }

    public void start() {
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newFixedThreadPool(1),
                Executors.newCachedThreadPool()
        ));

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = new DefaultChannelPipeline();
                pipeline.addLast("ssl", new SslHandler(getSSLEngine()));
                pipeline.addLast("decoder", new LumberjackDecoder());
                pipeline.addLast("logHandler", new LogEventHandler(eventListener));
                return pipeline;
            }
        });
        bootstrap.bind(new InetSocketAddress(configuration.getIpAddress(), configuration.getPort()));
    }

    private SSLEngine getSSLEngine() {
        SSLContext context = null;
        char[] storepass = configuration.getKeyStorePass().toCharArray();
//        char[] keypass = "infinera".toCharArray();
        String storePath = configuration.getKeyStorePath();

        try {
            context = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            FileInputStream fin = new FileInputStream(storePath);
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fin, storepass);

            kmf.init(ks, storepass);
            context.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(false);
        return engine;
    }

    public void stop() {
        bootstrap.shutdown();
    }
}
