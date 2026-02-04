package com.old.silence.job.client.core.handler.add;

import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.JobTaskType;


public class ClusterAddHandler extends AddHandler<ClusterAddHandler> {

    public ClusterAddHandler() {
        this(JobTaskType.CLUSTER);
    }

    public ClusterAddHandler(JobTaskType taskType) {
        super(taskType);
        // 集群模式只允许并发为 1
        setParallelNum(1);
        setR(this);
    }

    @Override
    public ClusterAddHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        super.setRouteKey(algorithmEnum);
        return this;
    }

    @Override
    public ClusterAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
