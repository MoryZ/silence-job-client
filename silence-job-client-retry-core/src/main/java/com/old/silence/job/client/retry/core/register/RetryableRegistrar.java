package com.old.silence.job.client.retry.core.register;

import org.springframework.stereotype.Component;

import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.retry.core.Scanner;
import com.old.silence.job.client.retry.core.cache.RetryerInfoCache;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;

import java.util.List;
import java.util.Objects;


@Component
public class RetryableRegistrar implements Lifecycle {

    private final List<Scanner> scanners;

    public RetryableRegistrar(List<Scanner> scanners) {
        this.scanners = scanners;
    }

    public void registerRetryHandler(RetryerInfo retryerInfo) {

        if (Objects.nonNull(RetryerInfoCache.get(retryerInfo.getScene(), retryerInfo.getExecutorClassName()))) {
            throw new SilenceRetryClientException("类:[{}]中已经存在场景:[{}]", retryerInfo.getExecutorClassName(), retryerInfo.getScene());
        }

        RetryerInfoCache.put(retryerInfo);
    }

    public void registerRetryHandler(List<RetryerInfo> contextList) {
        for (RetryerInfo retryerInfo : contextList) {
            registerRetryHandler(retryerInfo);
        }
    }

    @Override
    public void start() {
        for (Scanner scanner : scanners) {
            this.registerRetryHandler(scanner.doScan());
        }
    }

    @Override
    public void close() {
    }
}
