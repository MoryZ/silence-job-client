package com.old.silence.job.client.retry.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;

public class SimpleSilenceRetryListener implements SilenceJobListener {


    private static final Logger log = LoggerFactory.getLogger(SimpleSilenceRetryListener.class);

    @Override
    public void beforeRetry(String sceneName, String executorClassName, Object[] params) {
        log.debug("------> beforeRetry sceneName:[{}] executorClassName:[{}] params:[{}]",
                sceneName, executorClassName, JSON.toJSONString(params));
    }

    @Override
    public void successOnRetry(Object result, String sceneName, String executorClassName) {
        log.debug("------> successOnRetry sceneName:[{}] executorClassName:[{}] result:[{}]",
                sceneName, executorClassName, JSON.toJSONString(result));
    }

    @Override
    public void failureOnRetry(String sceneName, String executorClassName, Throwable e) {
        log.debug("------> failureOnRetry sceneName:[{}] executorClassName:[{}]", sceneName, executorClassName, e);
    }
}
