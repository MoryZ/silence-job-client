package com.old.silence.job.client.common.log.report;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.LogReport;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.window.SlidingWindow;
import com.old.silence.job.common.dto.LogTaskDTO;
import com.old.silence.job.common.window.Listener;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.log.dto.LogContentDTO;

import java.util.Objects;


public abstract class AbstractLogReport<T extends LogTaskDTO> implements Lifecycle, InitializingBean, LogReport {

    @Autowired
    private SilenceJobProperties silenceJobProperties;
    private SlidingWindow<LogTaskDTO> slidingWindow;

    @Override
    public void report(LogContentDTO logContentDTO) {
        slidingWindow.add(buildLogTaskDTO(logContentDTO));
    }

    protected abstract T buildLogTaskDTO(LogContentDTO logContentDTO);

    @Override
    public void start() {
        if (Objects.nonNull(slidingWindow)) {
            return;
        }

        SilenceJobProperties.LogSlidingWindowConfig logSlidingWindow = silenceJobProperties.getLogSlidingWindow();

        Listener<LogTaskDTO> reportLogListener = new ReportLogListener();
        slidingWindow = SlidingWindow
                .Builder
                .<LogTaskDTO>newBuilder()
                .withTotalThreshold(logSlidingWindow.getTotalThreshold())
                .withWindowTotalThreshold(logSlidingWindow.getWindowTotalThreshold())
                .withDuration(logSlidingWindow.getDuration(), logSlidingWindow.getChronoUnit())
                .withListener(reportLogListener)
                .build();

        slidingWindow.start();
    }

    @Override
    public void close() {
        if (Objects.isNull(slidingWindow)) {
            return;
        }

        SilenceJobLog.LOCAL.info("AsyncReport Log about to shutdown");
        slidingWindow.end();
        SilenceJobLog.LOCAL.info("AsyncReport Log has been shutdown");
    }

    @Override
    public void afterPropertiesSet() {
        LogReportFactory.add(this);
    }
}
