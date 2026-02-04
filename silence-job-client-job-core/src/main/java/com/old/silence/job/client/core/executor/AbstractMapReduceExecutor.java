package com.old.silence.job.client.core.executor;

import cn.hutool.core.lang.Assert;

import com.old.silence.job.client.core.dto.JobArgs;
import com.old.silence.job.client.core.dto.MergeReduceArgs;
import com.old.silence.job.client.core.dto.ReduceArgs;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.enums.MapReduceStage;
import com.old.silence.job.common.exception.SilenceJobMapReduceException;
import com.old.silence.job.common.model.JobContext;


public abstract class AbstractMapReduceExecutor extends AbstractMapExecutor {

    @Override
    public ExecuteResult doJobExecute(JobArgs jobArgs) {
        JobContext jobContext = JobContextManager.getJobContext();
        Assert.notNull(jobContext.getMrStage(), "请确认服务器当前定时任务的[任务类型]为MapReduce");
        if (jobContext.getMrStage().equals(MapReduceStage.MAP)) {
            return super.doJobExecute(jobArgs);
        } else if (jobContext.getMrStage().equals(MapReduceStage.REDUCE)) {
            ReduceArgs reduceArgs = (ReduceArgs) jobArgs;
            return this.doReduceExecute(reduceArgs);
        } else if (jobContext.getMrStage().equals(MapReduceStage.MERGE_REDUCE)) {
            MergeReduceArgs reduceArgs = (MergeReduceArgs) jobArgs;
            return this.doMergeReduceExecute(reduceArgs);
        }

        throw new SilenceJobMapReduceException("非法的MapReduceStage");
    }

    protected abstract ExecuteResult doReduceExecute(ReduceArgs reduceArgs);

    protected abstract ExecuteResult doMergeReduceExecute(MergeReduceArgs mergeReduceArgs);
}
