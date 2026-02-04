package com.old.silence.job.client.core.executor;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.old.silence.job.client.common.SilenceJobLogThreadLocal;
import com.old.silence.job.client.common.SilenceThreadLocal;
import com.old.silence.job.client.common.threadlocal.CommonThreadLocal;
import com.old.silence.job.common.model.JobContext;

import java.util.Objects;


public final class JobContextManager {
    private JobContextManager() {}

    private static final SilenceThreadLocal<JobContext> JOB_CONTEXT_LOCAL = initThreadLocal();
    private static SilenceThreadLocal<JobContext> initThreadLocal() {
        SilenceThreadLocal<JobContext> silenceThreadLocal = ServiceLoaderUtil.loadFirst(SilenceJobLogThreadLocal.class);
        if (Objects.isNull(silenceThreadLocal)) {
            silenceThreadLocal = new CommonThreadLocal<>(new ThreadLocal<>());
        }
        return silenceThreadLocal;
    }

    public static void setJobContext(JobContext jobContext) {
        JOB_CONTEXT_LOCAL.set(jobContext);
    }

    public static JobContext getJobContext() {
        return JOB_CONTEXT_LOCAL.get();
    }

    public static void removeJobContext() {
        JOB_CONTEXT_LOCAL.remove();
    }
}
