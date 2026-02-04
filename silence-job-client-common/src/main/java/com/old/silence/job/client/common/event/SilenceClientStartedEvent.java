package com.old.silence.job.client.common.event;

import org.springframework.context.ApplicationEvent;

public class SilenceClientStartedEvent extends ApplicationEvent {
    private static final String SOURCE = "SilenceJobStarted";

    public SilenceClientStartedEvent() {
        super(SOURCE);
    }
}
