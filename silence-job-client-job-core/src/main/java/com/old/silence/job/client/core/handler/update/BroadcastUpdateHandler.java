package com.old.silence.job.client.core.handler.update;

import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class BroadcastUpdateHandler extends UpdateHandler<BroadcastUpdateHandler>{

    public BroadcastUpdateHandler(BigInteger jobId) {
        super(JobTaskType.BROADCAST, jobId);
        setR(this);
    }

    @Override
    public BroadcastUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
