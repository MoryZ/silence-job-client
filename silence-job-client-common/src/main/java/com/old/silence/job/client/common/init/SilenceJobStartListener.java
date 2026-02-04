package com.old.silence.job.client.common.init;

import org.slf4j.helpers.MessageFormatter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.event.SilenceClientStartedEvent;
import com.old.silence.job.client.common.event.SilenceClientStartingEvent;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.util.SilenceJobVersion;
import com.old.silence.job.log.SilenceJobLog;

import java.util.List;

/**
 * 系统启动监听器
 *
 */
@Component
public class SilenceJobStartListener implements ApplicationRunner {
    private final List<Lifecycle> lifecycleList;
    private volatile boolean isStarted = false;

    public SilenceJobStartListener(List<Lifecycle> lifecycleList) {
        this.lifecycleList = lifecycleList;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isStarted) {
            SilenceJobLog.LOCAL.info("silence-job client already started v{}", SilenceJobVersion.getVersion());
            return;
        }

        System.out.println(MessageFormatter.format(SystemConstants.LOGO, SilenceJobVersion.getVersion()).getMessage());
        SilenceJobLog.LOCAL.info("silence-job client is preparing to start... v{}", SilenceJobVersion.getVersion());
        SilenceSpringContext.getContext().publishEvent(new SilenceClientStartingEvent());
        lifecycleList.forEach(Lifecycle::start);
        SilenceSpringContext.getContext().publishEvent(new SilenceClientStartedEvent());
        isStarted = true;
        SilenceJobLog.LOCAL.info("silence-job client started successfully v{}", SilenceJobVersion.getVersion());
    }

}
