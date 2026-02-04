package com.old.silence.job.client.core.executor;

import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.core.IJobExecutor;
import com.old.silence.job.client.core.cache.FutureCache;
import com.old.silence.job.client.core.cache.ThreadPoolCache;
import com.old.silence.job.client.core.dto.JobArgs;
import com.old.silence.job.client.core.dto.MapArgs;
import com.old.silence.job.client.core.dto.MergeReduceArgs;
import com.old.silence.job.client.core.dto.ReduceArgs;
import com.old.silence.job.client.core.dto.ShardingJobArgs;
import com.old.silence.job.client.core.log.JobLogMeta;
import com.old.silence.job.client.core.timer.StopTaskTimerTask;
import com.old.silence.job.client.core.timer.TimerManager;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.enums.MapReduceStage;
import com.old.silence.job.common.model.JobArgsHolder;
import com.old.silence.job.common.model.JobContext;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.old.silence.job.common.util.JsonUtils;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 广播模式
 *
 */

public abstract class AbstractJobExecutor implements IJobExecutor {

    @Override
    public void jobExecute(JobContext jobContext) {

        // 创建可执行的任务
        Integer parallelNum = Optional.ofNullable(jobContext.getParallelNum()).orElse(1);
        ThreadPoolExecutor threadPool = ThreadPoolCache.createThreadPool(jobContext.getTaskBatchId(), Math.max(1, parallelNum));
        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(threadPool);

        // 将任务添加到时间轮中，到期停止任务
        TimerManager.add(new StopTaskTimerTask(jobContext.getTaskBatchId()), jobContext.getExecutorTimeout(), TimeUnit.SECONDS);

        jobContext.setChangeWfContext(Maps.newConcurrentMap());
        // 执行任务
        ListenableFuture<ExecuteResult> submit = decorator.submit(() -> {
            JobArgs jobArgs;
            if (jobContext.getTaskType().equals(JobTaskType.SHARDING)) {

                jobArgs = buildShardingJobArgs(jobContext);
            } else if (List.of(JobTaskType.MAP_REDUCE, JobTaskType.MAP)
                    .contains(jobContext.getTaskType())) {
                if (MapReduceStage.MAP.equals(jobContext.getMrStage())) {
                    jobArgs = buildMapJobArgs(jobContext);
                } else if (MapReduceStage.REDUCE.equals(jobContext.getMrStage())) {
                    jobArgs = buildReduceJobArgs(jobContext);
                } else {
                    jobArgs = buildMergeReduceJobArgs(jobContext);
                }

            } else {
                jobArgs = buildJobArgs(jobContext);
            }

            jobArgs.setWfContext(jobContext.getWfContext());
            jobArgs.setChangeWfContext(jobContext.getChangeWfContext());
            jobArgs.setJobId(jobContext.getJobId());

            try {
                // 初始化调度信息（日志上报LogUtil）
                initLogContext(jobContext);
                return doJobExecute(jobArgs);
            } finally {
                SilenceJobLogManager.removeLogMeta();
                JobContextManager.removeJobContext();
            }

        });

        FutureCache.addFuture(jobContext.getTaskBatchId(), submit);
        Futures.addCallback(submit, new JobExecutorFutureCallback(jobContext), decorator);
    }

    private void initLogContext(JobContext jobContext) {
        JobLogMeta logMeta = new JobLogMeta();
        logMeta.setNamespaceId(jobContext.getNamespaceId());
        logMeta.setTaskId(jobContext.getTaskId());
        logMeta.setGroupName(jobContext.getGroupName());
        logMeta.setJobId(jobContext.getJobId());
        logMeta.setTaskBatchId(jobContext.getTaskBatchId());
        SilenceJobLogManager.initLogInfo(logMeta, LogTypeEnum.JOB);
        JobContextManager.setJobContext(jobContext);
    }

    private static JobArgs buildJobArgs(JobContext jobContext) {
        JobArgs jobArgs = new JobArgs();
        jobArgs.setJobParams(jobContext.getJobArgsHolder().getJobParams());
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setTaskBatchId(jobContext.getTaskBatchId());
        return jobArgs;
    }

    private static JobArgs buildShardingJobArgs(JobContext jobContext) {
        ShardingJobArgs jobArgs = new ShardingJobArgs();
        jobArgs.setJobParams(jobContext.getJobArgsHolder().getJobParams());
        jobArgs.setTaskBatchId(jobContext.getTaskBatchId());
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setShardingIndex(jobContext.getShardingIndex());
        jobArgs.setShardingTotal(jobContext.getShardingTotal());
        return jobArgs;
    }

    private static JobArgs buildMapJobArgs(JobContext jobContext) {
        MapArgs jobArgs = new MapArgs();
        JobArgsHolder jobArgsHolder = jobContext.getJobArgsHolder();
        jobArgs.setJobParams(jobArgsHolder.getJobParams());
        jobArgs.setMapResult(jobArgsHolder.getMaps());
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setTaskName(jobContext.getTaskName());
        jobArgs.setTaskBatchId(jobContext.getTaskBatchId());
        return jobArgs;
    }

    private static JobArgs buildReduceJobArgs(JobContext jobContext) {
        ReduceArgs jobArgs = new ReduceArgs();
        JobArgsHolder jobArgsHolder = jobContext.getJobArgsHolder();
        jobArgs.setJobParams(jobArgsHolder.getJobParams());
        Object maps = jobArgsHolder.getMaps();
        if (Objects.nonNull(maps)) {
            if (maps instanceof String) {
                jobArgs.setMapResult(JsonUtils.parseList((String) maps, Object.class));
            } else {
                jobArgs.setMapResult((List<?>) maps);
            }
        }
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setTaskBatchId(jobContext.getTaskBatchId());
        jobArgs.setWfContext(jobContext.getWfContext());
        return jobArgs;
    }

    private static JobArgs buildMergeReduceJobArgs(JobContext jobContext) {
        MergeReduceArgs jobArgs = new MergeReduceArgs();
        JobArgsHolder jobArgsHolder = jobContext.getJobArgsHolder();
        jobArgs.setJobParams(jobArgsHolder.getJobParams());
        Object reduces = jobArgsHolder.getReduces();
        if (Objects.nonNull(reduces)) {
            if (reduces instanceof String) {
                jobArgs.setReduces(JsonUtils.parseList((String) reduces, Object.class));
            } else {
                jobArgs.setReduces((List<?>) reduces);
            }
        }

        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setTaskBatchId(jobContext.getTaskBatchId());
        jobArgs.setWfContext(jobContext.getWfContext());
        return jobArgs;
    }

    protected abstract ExecuteResult doJobExecute(JobArgs jobArgs);
}
