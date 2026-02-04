package com.old.silence.job.client.retry.core.callback.complete;


public interface RetryCompleteCallback {

    void doSuccessCallback(String sceneName, String executorName, Object[] params);

    void doMaxRetryCallback(String sceneName, String executorName, Object[] params);
}
