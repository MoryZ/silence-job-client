package com.old.silence.job.client.core.dto;



public class ShardingJobArgs extends JobArgs {

    private Integer shardingTotal;

    private Integer shardingIndex;

    public Integer getShardingTotal() {
        return shardingTotal;
    }

    public void setShardingTotal(Integer shardingTotal) {
        this.shardingTotal = shardingTotal;
    }

    public Integer getShardingIndex() {
        return shardingIndex;
    }

    public void setShardingIndex(Integer shardingIndex) {
        this.shardingIndex = shardingIndex;
    }
}
