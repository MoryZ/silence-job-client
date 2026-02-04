package com.old.silence.job.client.retry.core.strategy;

import org.springframework.stereotype.Component;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import com.old.silence.job.client.retry.core.RetryExecutor;
import com.old.silence.job.client.retry.core.RetryExecutorParameter;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.client.retry.core.retryer.RetryType;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.retryer.RetryerResultContext;
import com.old.silence.job.common.enums.RetryResultStatus;
import com.old.silence.job.log.SilenceJobLog;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 执行远程重试
 *
 */
@Component

public class RemoteRetryStrategies extends AbstractRetryStrategies {

    @Override
    public boolean supports(int stage, RetryType retryType) {
        return RetrySiteSnapshot.EnumStage.REMOTE.getStage() == stage;
    }

    @Override
    protected void setStage() {
        RetrySiteSnapshot.setStage(RetrySiteSnapshot.EnumStage.REMOTE.getStage());
    }

    @Override
    protected Consumer<Object> doRetrySuccessConsumer(RetryerResultContext context) {
        return o -> {
            SilenceJobLog.LOCAL.debug("RemoteRetryStrategies doRetrySuccessConsumer ");
        };
    }

    @Override
    protected void error(RetryerResultContext context) {
        context.setRetryResultStatus(RetryResultStatus.FAILURE);
    }

    @Override
    protected boolean preValidator(RetryerInfo retryerInfo, RetryerResultContext resultContext) {
        if (RetrySiteSnapshot.isRunning()) {
            resultContext.setRetryResultStatus(RetryResultStatus.FAILURE);
            resultContext.setMessage("执行重试检验不通过 原因: 存在正在运行的重试任务");
            return false;
        }

        return true;
    }

    @Override
    protected void unexpectedError(Exception e, RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatus(RetryResultStatus.SUCCESS);
    }

    @Override
    protected void success(RetryerResultContext retryerResultContext) {
        retryerResultContext.setRetryResultStatus(RetryResultStatus.SUCCESS);
    }

    @Override
    protected Consumer<Throwable> doGetRetryErrorConsumer(RetryerInfo retryerInfo, Object[] params) {
        return throwable -> {
            SilenceJobLog.LOCAL.debug("RemoteRetryStrategies doGetRetryErrorConsumer ");
        };
    }

    @Override
    protected Callable doGetCallable(RetryExecutor<WaitStrategy, StopStrategy> retryExecutor, Object... params) {
        return () -> retryExecutor.execute(params);
    }

    @Override
    protected RetryExecutorParameter<WaitStrategy, StopStrategy> getRetryExecutorParameter(RetryerInfo retryerInfo) {
        return new RetryExecutorParameter<WaitStrategy, StopStrategy>() {

            @Override
            public WaitStrategy backOff() {
                return WaitStrategies.fixedWait(1, TimeUnit.SECONDS);
            }

            @Override
            public StopStrategy stop() {
                return StopStrategies.stopAfterAttempt(1);
            }

            @Override
            public List<RetryListener> getRetryListeners() {
                return Collections.singletonList(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        Integer attemptNumber = RetrySiteSnapshot.getAttemptNumber();
                        if (attempt.hasResult()) {
                            SilenceJobLog.LOCAL.info("silence-job 远程重试成功，第[{}]次调度", attemptNumber);
                        }

                        if (attempt.hasException()) {
                            SilenceJobLog.LOCAL.error("silence-job 远程重试失败，第[{}]次调度 ", attemptNumber, attempt.getExceptionCause());
                        }

                    }
                });
            }
        };
    }

}
