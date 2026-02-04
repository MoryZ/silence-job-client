package com.old.silence.job.client.core.handler.update;

import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class ClusterUpdateHandler extends UpdateHandler<ClusterUpdateHandler> {

    public ClusterUpdateHandler(BigInteger jobId) {
        super(JobTaskType.CLUSTER, jobId);
        setR(this);
    }

    @Override
    public ClusterUpdateHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        return super.setRouteKey(algorithmEnum);
    }

    @Override
    public ClusterUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
