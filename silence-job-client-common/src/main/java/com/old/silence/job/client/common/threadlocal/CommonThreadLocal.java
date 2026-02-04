package com.old.silence.job.client.common.threadlocal;

import cn.hutool.core.lang.Assert;
import com.old.silence.job.client.common.SilenceThreadLocal;
import com.old.silence.job.client.common.exception.SilenceJobClientException;

/**
 * ThreadLocal实现类
 *
 */
public class CommonThreadLocal<T> implements SilenceThreadLocal<T> {

    private final ThreadLocal<T> threadLocal;

    public CommonThreadLocal(ThreadLocal<T> threadLocal) {
        Assert.notNull(threadLocal, () -> new SilenceJobClientException("thread local can not be null"));
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
