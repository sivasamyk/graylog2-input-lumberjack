package org.lumberjack.server;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.List;

/**
 * Created on 9/4/15.
 */
public class LogEventHandler extends SimpleChannelHandler {

    private LogEventListener eventListener;

    public LogEventHandler(LogEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.messageReceived(ctx, e);
        Object message = e.getMessage();

        if(message != null)
        {
            List<LogEvent> events = (List<LogEvent>)message;
            eventListener.onEvents(events);
        }
    }
}
