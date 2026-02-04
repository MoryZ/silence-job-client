package com.old.silence.job.client.core.handler.add;

import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.JobTaskType;


public class BroadcastAddHandler extends AddHandler<BroadcastAddHandler> {

    public BroadcastAddHandler() {
        this(JobTaskType.BROADCAST);
    }

    public BroadcastAddHandler(JobTaskType taskType) {
        super(taskType);
        // 广播模式只允许并发为 1
        setParallelNum(1);
        // 广播模式采用轮询模式
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public BroadcastAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
