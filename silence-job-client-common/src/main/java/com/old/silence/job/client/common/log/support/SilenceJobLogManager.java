package com.old.silence.job.client.common.log.support;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.old.silence.job.client.common.SilenceJobLogThreadLocal;
import com.old.silence.job.client.common.SilenceThreadLocal;
import com.old.silence.job.client.common.log.report.LogMeta;
import com.old.silence.job.client.common.threadlocal.CommonThreadLocal;
import com.old.silence.job.log.enums.LogTypeEnum;

import java.util.Objects;


public final class SilenceJobLogManager {

    private static final SilenceThreadLocal<LogTypeEnum> LOG_TYPE = silenceJobLogContextLoader();
    private static final SilenceThreadLocal<LogMeta> LOG_META = silenceJobLogContextLoader();

    private static <T> SilenceThreadLocal<T> silenceJobLogContextLoader() {
        SilenceThreadLocal<T> silenceThreadLocal = ServiceLoaderUtil.loadFirst(SilenceJobLogThreadLocal.class);
        if (Objects.isNull(silenceThreadLocal)) {
            silenceThreadLocal = new CommonThreadLocal<>(new ThreadLocal<>());
        }
        return silenceThreadLocal;
    }

    private SilenceJobLogManager() {
    }

    public static void initLogInfo(LogMeta logMeta, LogTypeEnum logType) {
        setLogMeta(logMeta);
        setLogType(logType);
    }

    public static void setLogMeta(LogMeta logMeta) {
        LOG_META.set(logMeta);
    }

    public static LogMeta getLogMeta() {
        return LOG_META.get();
    }

    public static void removeLogMeta() {
        LOG_META.remove();
    }

    public static void removeAll() {
        removeLogMeta();
        removeLogType();
    }

    public static void setLogType(LogTypeEnum logType) {
        LOG_TYPE.set(logType);
    }

    public static LogTypeEnum getLogType() {
        return LOG_TYPE.get();
    }

    public static void removeLogType() {
        LOG_TYPE.remove();
    }

}
