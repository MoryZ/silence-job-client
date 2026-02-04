package com.old.silence.job.client.common.event;

import org.springframework.context.ApplicationEvent;


public class SilenceServerStartFailedEvent extends ApplicationEvent {

    private static final String SOURCE = "SilenceServerStartFailed";

    public SilenceServerStartFailedEvent() {
        super(SOURCE);
    }

}
