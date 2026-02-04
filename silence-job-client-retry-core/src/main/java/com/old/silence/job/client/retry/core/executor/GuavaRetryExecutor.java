package com.old.silence.job.client.retry.core.executor;

import cn.hutool.core.lang.Assert;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategy;
import com.old.silence.job.client.retry.core.RetryExecutorParameter;
import com.old.silence.job.client.retry.core.cache.RetryerInfoCache;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.log.SilenceJobLog;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Guava 重试执行器
 *
 */

public class GuavaRetryExecutor extends AbstractRetryExecutor<WaitStrategy, StopStrategy> {

    public GuavaRetryExecutor(String sceneName, String executorClassName) {
        retryerInfo = RetryerInfoCache.get(sceneName, executorClassName);
        Assert.notNull(retryerInfo, () -> new SilenceRetryClientException("retryerInfo is null sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName));
    }

    public GuavaRetryExecutor() {
    }

    @Override
    public Retryer build(RetryExecutorParameter<WaitStrategy, StopStrategy> parameter) {

        RetryerBuilder<Object> retryerBuilder = RetryerBuilder.newBuilder();
        retryerBuilder.retryIfException(throwable -> true);
        retryerBuilder.withWaitStrategy(parameter.backOff());
        retryerBuilder.withStopStrategy(parameter.stop());
        for (RetryListener retryListener : parameter.getRetryListeners()) {
            retryerBuilder.withRetryListener(retryListener);
        }

        return retryerBuilder.build();
    }

    @Override
    public <V> V call(Retryer<V> retryer, Callable<V> callable, Consumer<Throwable> retryError, Consumer<V> retrySuccess) throws Exception {

        V result = null;
        try {
            result = retryer.call(callable);
            retrySuccess.accept(result);
        } catch (RetryException e) {
            // 重试完成，仍然失败
            SilenceJobLog.LOCAL.debug("业务系统重试异常：", e.getLastFailedAttempt().getExceptionCause());
            retryError.accept(e.getLastFailedAttempt().getExceptionCause());
        }

        return result;
    }
}
