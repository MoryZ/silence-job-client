package com.old.silence.job.client.retry.core.strategy;


import com.old.silence.job.client.retry.core.retryer.RetryType;
import com.old.silence.job.client.retry.core.retryer.RetryerResultContext;

public interface RetryStrategy {

    boolean supports(int stage, RetryType retryType);

    RetryerResultContext openRetry(String sceneName, String executorClassName, Object[] params);

}
