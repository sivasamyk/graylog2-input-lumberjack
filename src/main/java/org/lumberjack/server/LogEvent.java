package org.lumberjack.server;

import java.util.Map;

/**
 * Created on 9/4/15.
 */
public class LogEvent {

    private Map<String,String> eventData;

    public LogEvent(Map<String,String> eventData) {
        this.eventData = eventData;
    }

    public Map<String, String> getEventData() {
        return eventData;
    }
}
