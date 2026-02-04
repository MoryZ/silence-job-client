package com.old.silence.job.client.common.event;

import org.springframework.context.ApplicationEvent;


public class SilenceClientClosedEvent extends ApplicationEvent {
    private static final String SOURCE = "SilenceJobClosed";

    public SilenceClientClosedEvent() {
        super(SOURCE);
    }
}
