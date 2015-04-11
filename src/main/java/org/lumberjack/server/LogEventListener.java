package org.lumberjack.server;

import java.util.List;

/**
 * Created on 9/4/15.
 */
public interface LogEventListener {
    void onEvents(List<LogEvent> logEvents);
}
