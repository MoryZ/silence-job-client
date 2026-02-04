package com.old.silence.job.client.retry.core.report;

import org.springframework.stereotype.Component;
import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.window.SlidingWindow;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 异步上报数据
 *
 */
@Component
public class AsyncReport extends AbstractReport implements Lifecycle {
    private SlidingWindow<RetryTaskDTO> slidingWindow;

    @Override
    public boolean supports(boolean async) {
        return async;
    }

    @Override
    public boolean doReport(RetryerInfo retryerInfo, Object[] params) {

        return syncReport(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params, retryerInfo.getTimeout(),
                retryerInfo.getUnit());
    }

    /**
     * 异步上报到服务端, 若当前处于远程重试阶段不会进行执行上报
     */
    public Boolean syncReport(String scene, String targetClassName, Object[] args, long timeout, TimeUnit unit) {

        RetryTaskDTO retryTaskDTO = buildRetryTaskDTO(scene, targetClassName, args);
        slidingWindow.add(retryTaskDTO);
        return Boolean.TRUE;
    }

    @Override
    public void start() {

        SilenceJobProperties.SlidingWindowConfig slidingWindowConfig = silenceJobProperties.getRetry().getReportSlidingWindow();

        slidingWindow = SlidingWindow
                .Builder
                .<RetryTaskDTO>newBuilder()
                .withTotalThreshold(slidingWindowConfig.getTotalThreshold())
                .withWindowTotalThreshold(slidingWindowConfig.getWindowTotalThreshold())
                .withDuration(slidingWindowConfig.getDuration(), slidingWindowConfig.getChronoUnit())
                .withListener(new ReportListener())
                .build();

        slidingWindow.start();
    }

    @Override
    public void close() {
        SilenceJobLog.LOCAL.info("AsyncReport about to shutdown");
        if (Objects.nonNull(slidingWindow)) {
            slidingWindow.end();
        }
        SilenceJobLog.LOCAL.info("AsyncReport has been shutdown");
    }
}
