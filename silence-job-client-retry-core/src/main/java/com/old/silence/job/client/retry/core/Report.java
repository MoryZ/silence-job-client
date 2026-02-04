package com.old.silence.job.client.retry.core;


public interface Report {

    boolean supports(boolean async);

    boolean report(String scene, final String targetClassName, final Object[] args);
}
