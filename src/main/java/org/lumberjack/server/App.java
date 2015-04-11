package org.lumberjack.server;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        LogEventListener logEventListener = new LogEventListener() {
            public void onEvents(List<LogEvent> logEvents) {
                for(LogEvent event : logEvents) {
                   // System.out.println(event);
                }
            }
        };
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setPort(5043);
        configuration.setKeyStorePass("password");
        configuration.setKeyStorePass("/path/to/keystore.jks");
        new LumberjackServer(configuration,logEventListener).start();
    }
}
