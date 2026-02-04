package com.old.silence.job.client.common.event;

import org.springframework.context.ApplicationEvent;


public class SilenceClientStartingEvent extends ApplicationEvent {
    private static final String SOURCE = "SilenceJobStarting";

    public SilenceClientStartingEvent() {
        super(SOURCE);
    }
}
