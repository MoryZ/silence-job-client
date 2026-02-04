package com.old.silence.job.client.common.event;

import org.springframework.context.ApplicationEvent;


public class SilenceClientClosingEvent extends ApplicationEvent {
    private static final String SOURCE = "SilenceJobClosing";

    public SilenceClientClosingEvent() {
        super(SOURCE);
    }
}
