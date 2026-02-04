package com.old.silence.job.client.core.handler.add;

import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.JobTaskType;


public class MapAddHandler extends AddHandler<MapAddHandler> {

    public MapAddHandler() {
        this(JobTaskType.MAP);
    }

    public MapAddHandler(JobTaskType taskType) {
        super(taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapAddHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
