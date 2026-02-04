package com.old.silence.job.client.core.dto;

import jakarta.validation.constraints.NotNull;


import java.math.BigInteger;



public class JobTriggerDTO {

    @NotNull(message = "jobId 不能为空")
    private BigInteger jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;

    public BigInteger getJobId() {
        return jobId;
    }

    public void setJobId(BigInteger jobId) {
        this.jobId = jobId;
    }

    public String getTmpArgsStr() {
        return tmpArgsStr;
    }

    public void setTmpArgsStr(String tmpArgsStr) {
        this.tmpArgsStr = tmpArgsStr;
    }
}
