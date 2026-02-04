package com.old.silence.job.client.common.event;

import org.springframework.context.ApplicationEvent;


public class SilenceChannelReconnectEvent extends ApplicationEvent {
    private static final String SOURCE = "ChannelReconnect";

    public SilenceChannelReconnectEvent() {
        super(SOURCE);
    }
}
