package com.old.silence.job.client.core.handler.update;

import com.old.silence.job.common.enums.JobTaskType;

import java.math.BigInteger;

public class ShardingUpdateHandler extends UpdateHandler<ShardingUpdateHandler>{

    public ShardingUpdateHandler(BigInteger jobId) {
        super(JobTaskType.SHARDING, jobId);
        setR(this);
    }

    @Override
    public ShardingUpdateHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }

    @Override
    public ShardingUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
