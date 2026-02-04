package com.old.silence.job.client.core.register;


import org.springframework.stereotype.Component;

import com.old.silence.job.client.common.Lifecycle;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.client.core.Scanner;
import com.old.silence.job.client.core.cache.JobExecutorInfoCache;
import com.old.silence.job.client.core.dto.JobExecutorInfo;

import java.util.List;

@Component
public class JobExecutorRegistrar implements Lifecycle {
    private final List<Scanner> scanners;

    public JobExecutorRegistrar(List<Scanner> scanners) {
        this.scanners = scanners;
    }

    public void registerRetryHandler(JobExecutorInfo jobExecutorInfo) {

        if (JobExecutorInfoCache.isExisted(jobExecutorInfo.getExecutorName())) {
            throw new SilenceJobClientException("不允许执行器名称重复: {}", jobExecutorInfo.getExecutorName());
        }

        JobExecutorInfoCache.put(jobExecutorInfo);
    }

    public void registerRetryHandler(List<JobExecutorInfo> contextList) {
        for (JobExecutorInfo jobExecutorInfo : contextList) {
            registerRetryHandler(jobExecutorInfo);
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
