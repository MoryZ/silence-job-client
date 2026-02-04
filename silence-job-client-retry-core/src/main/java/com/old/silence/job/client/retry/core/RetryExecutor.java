package com.old.silence.job.client.retry.core;

import com.github.rholder.retry.Retryer;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;

import java.util.concurrent.Callable;
import java.util.function.Consumer;


public interface RetryExecutor<BR, SR> {

    RetryerInfo getRetryerInfo();

    Object execute(Object... params);


    Retryer build(RetryExecutorParameter<BR, SR> parameter);

    <V> V call(Retryer<V> retryer, Callable<V> callable, Consumer<Throwable> retryError, Consumer<V> retrySuccess) throws Exception;

}
