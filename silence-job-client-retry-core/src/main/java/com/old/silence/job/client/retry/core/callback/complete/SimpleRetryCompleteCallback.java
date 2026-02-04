package com.old.silence.job.client.retry.core.callback.complete;


import org.springframework.stereotype.Component;


@Component

public class SimpleRetryCompleteCallback implements RetryCompleteCallback {

    @Override
    public void doSuccessCallback(String sceneName, String executorName, Object[] params) {

    }

    @Override
    public void doMaxRetryCallback(String sceneName, String executorName, Object[] params) {
    }
}
