package com.old.silence.job.client.common;


public interface SilenceThreadLocal<T> {

    void set(T value);

    void remove();

    T get();

}
