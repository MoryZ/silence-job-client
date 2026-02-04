package com.old.silence.job.client.retry.core.intercepter;

import cn.hutool.core.lang.Assert;
import com.old.silence.job.client.retry.core.RetrySiteSnapshotContext;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;

/**
 * ThreadLocal实现类
 *
 */
public class ThreadLockRetrySiteSnapshotContext<T> implements RetrySiteSnapshotContext<T> {

    private final ThreadLocal<T> threadLocal;

    public ThreadLockRetrySiteSnapshotContext(ThreadLocal<T> threadLocal) {
        Assert.notNull(threadLocal, () -> new SilenceRetryClientException("thread local can not be null"));
        this.threadLocal = threadLocal;
    }

    @Override
    public void set(T value) {
        threadLocal.set(value);
    }

    @Override
    public void remove() {
        threadLocal.remove();
    }

    @Override
    public T get() {
        return threadLocal.get();
    }
}
