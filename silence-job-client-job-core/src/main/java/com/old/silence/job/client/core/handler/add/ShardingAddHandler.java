package com.old.silence.job.client.core.handler.add;

import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.JobTaskType;


public class ShardingAddHandler extends AddHandler<ShardingAddHandler> {

    public ShardingAddHandler() {
        this(JobTaskType.SHARDING);
    }

    public ShardingAddHandler(JobTaskType taskType) {
        super(taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public ShardingAddHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }

    @Override
    public ShardingAddHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
