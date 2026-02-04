package com.old.silence.job.client.retry.core.event;


public interface SilenceJobListener {
    void beforeRetry(String sceneName, String executorClassName, Object[] params);

    void successOnRetry(Object result, String sceneName, String executorClassName);

    void failureOnRetry(String sceneName, String executorClassName, Throwable e);
}
