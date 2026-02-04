package com.old.silence.job.client.retry.core.log;


import com.old.silence.job.client.common.log.report.LogMeta;

import java.math.BigInteger;



public class RetryLogMeta extends LogMeta {

    private BigInteger retryTaskId;

    private BigInteger retryId;

    public BigInteger getRetryTaskId() {
        return retryTaskId;
    }

    public void setRetryTaskId(BigInteger retryTaskId) {
        this.retryTaskId = retryTaskId;
    }

    public BigInteger getRetryId() {
        return retryId;
    }

    public void setRetryId(BigInteger retryId) {
        this.retryId = retryId;
    }
}
