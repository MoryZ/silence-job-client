package com.old.silence.job.client.core.handler.update;

import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class MapReduceUpdateHandler extends UpdateHandler<MapReduceUpdateHandler> {

    public MapReduceUpdateHandler(BigInteger jobId) {
        super(JobTaskType.MAP_REDUCE, jobId);
        setR(this);
    }

    @Override
    public MapReduceUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapReduceUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }

    @Override
    public MapReduceUpdateHandler setShardNum(Integer shardNum) {
        return super.setShardNum(shardNum);
    }
}
