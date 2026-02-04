package com.old.silence.job.client.retry.core;


/**
 * 重试现场记录上下文
 * 默认实现see: {@link com.old.silence.job.client.retry.intercepter.ThreadLockRetrySiteSnapshotContext}
 *
 */
public interface RetrySiteSnapshotContext<T> {

    void set(T value);

    void remove();

    T get();
}
