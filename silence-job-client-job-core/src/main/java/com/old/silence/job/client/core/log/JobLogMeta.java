package com.old.silence.job.client.core.log;


import com.old.silence.job.client.common.log.report.LogMeta;

import java.math.BigInteger;


public class JobLogMeta extends LogMeta {

    /**
     * 任务信息id
     */
    private BigInteger jobId;

    /**
     * 任务实例id
     */
    private BigInteger taskBatchId;

    /**
     * 调度任务id
     */
    private BigInteger taskId;

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public BigInteger getTaskBatchId() {
        return taskBatchId;
    }

    public void setTaskBatchId(BigInteger taskBatchId) {
        this.taskBatchId = taskBatchId;
    }

    public BigInteger getTaskId() {
        return taskId;
    }

    public void setTaskId(BigInteger taskId) {
        this.taskId = taskId;
    }
}
