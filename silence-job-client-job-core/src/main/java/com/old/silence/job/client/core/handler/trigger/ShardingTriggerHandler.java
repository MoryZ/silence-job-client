package com.old.silence.job.client.core.handler.trigger;


import java.math.BigInteger;

public class ShardingTriggerHandler extends TriggerJobHandler<ShardingTriggerHandler> {

    public ShardingTriggerHandler(BigInteger triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public ShardingTriggerHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }
}
