package com.old.silence.job.client.core.handler.add;

import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.JobTaskType;


public class MapReduceAddHandler extends AddHandler<MapReduceAddHandler> {

    public MapReduceAddHandler() {
        this(JobTaskType.MAP_REDUCE);
    }

    public MapReduceAddHandler(JobTaskType taskType) {
        super(taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapReduceAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapReduceAddHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }

    @Override
    public MapReduceAddHandler setShardNum(Integer shardNum) {
        return super.setShardNum(shardNum);
    }
}
