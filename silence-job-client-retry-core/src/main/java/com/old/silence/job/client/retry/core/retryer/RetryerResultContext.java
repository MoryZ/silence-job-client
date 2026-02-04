package com.old.silence.job.client.retry.core.retryer;

import com.old.silence.job.common.enums.RetryResultStatus;




public class RetryerResultContext {

    private Object result;

    private RetryerInfo retryerInfo;

    private RetryResultStatus retryResultStatus;

    private String message;

    private Throwable throwable;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public RetryerInfo getRetryerInfo() {
        return retryerInfo;
    }

    public void setRetryerInfo(RetryerInfo retryerInfo) {
        this.retryerInfo = retryerInfo;
    }

    public RetryResultStatus getRetryResultStatus() {
        return retryResultStatus;
    }

    public void setRetryResultStatus(RetryResultStatus retryResultStatus) {
        this.retryResultStatus = retryResultStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
