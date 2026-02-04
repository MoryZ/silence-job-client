package com.old.silence.job.client.retry.core.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;


public class RetryerInfoCache {

    private static Table<String, String, RetryerInfo> RETRY_HANDLER_REPOSITORY = HashBasedTable.create();

    public static RetryerInfo put(RetryerInfo retryerInfo) {
        return RETRY_HANDLER_REPOSITORY.put(retryerInfo.getScene(), retryerInfo.getExecutorClassName(), retryerInfo);
    }

    public static RetryerInfo get(String sceneName, String executorClassName) {
        return RETRY_HANDLER_REPOSITORY.get(sceneName, executorClassName);
    }

}
