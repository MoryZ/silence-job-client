package com.old.silence.job.client.common.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.event.SilenceClientClosedEvent;
import com.old.silence.job.client.common.event.SilenceClientClosingEvent;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.util.SilenceJobVersion;

import java.util.List;


/**
 * 系统关闭监听器
 *
 */
@Component
public class SilenceJobCloseListener implements ApplicationListener<ContextClosedEvent> {


    private static final Logger log = LoggerFactory.getLogger(SilenceJobCloseListener.class);
    private final List<Lifecycle> lifecycleList;

    public SilenceJobCloseListener(List<Lifecycle> lifecycleList) {
        this.lifecycleList = lifecycleList;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("silence-job client about to shutdown v{}", SilenceJobVersion.getVersion());
        SilenceSpringContext.getContext().publishEvent(new SilenceClientClosingEvent());
        lifecycleList.forEach(Lifecycle::close);
        SilenceSpringContext.getContext().publishEvent(new SilenceClientClosedEvent());
        log.info("silence-job client closed successfully v{}", SilenceJobVersion.getVersion());
    }
}
