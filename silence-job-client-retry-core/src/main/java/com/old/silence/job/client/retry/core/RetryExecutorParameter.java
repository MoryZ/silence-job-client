package com.old.silence.job.client.retry.core;

import com.github.rholder.retry.RetryListener;

import java.util.List;


public interface RetryExecutorParameter<BR, SR> {

    BR backOff();

    SR stop();

    List<RetryListener> getRetryListeners();

}
