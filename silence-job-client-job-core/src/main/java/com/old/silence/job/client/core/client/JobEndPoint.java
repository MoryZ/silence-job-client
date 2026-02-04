package com.old.silence.job.client.core.client;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.old.silence.core.enums.EnumValueFactory;
import com.old.silence.job.client.common.annotation.Mapping;
import com.old.silence.job.client.common.annotation.SilenceEndPoint;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.common.rpc.client.RequestMethod;
import com.old.silence.job.client.core.IJobExecutor;
import com.old.silence.job.client.core.cache.JobExecutorInfoCache;
import com.old.silence.job.client.core.cache.ThreadPoolCache;
import com.old.silence.job.client.core.dto.JobExecutorInfo;
import com.old.silence.job.client.core.executor.AbstractJobExecutor;
import com.old.silence.job.client.core.executor.AbstractMapExecutor;
import com.old.silence.job.client.core.executor.AbstractMapReduceExecutor;
import com.old.silence.job.client.core.executor.AnnotationJobExecutor;
import com.old.silence.job.client.core.executor.AnnotationMapJobExecutor;
import com.old.silence.job.client.core.executor.AnnotationMapReduceJobExecutor;

import com.old.silence.job.client.core.log.JobLogMeta;
import com.old.silence.job.common.client.dto.StopJobDTO;
import com.old.silence.job.common.client.dto.request.DispatchJobRequest;
import com.old.silence.job.common.enums.MapReduceStage;
import com.old.silence.job.log.enums.LogTypeEnum;

import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.JobArgsHolder;
import com.old.silence.job.common.model.JobContext;
import com.old.silence.job.log.SilenceJobLog;
import com.google.common.collect.Maps;
import com.old.silence.job.common.util.JsonUtils;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.JOB_DISPATCH;
import static com.old.silence.job.common.constant.SystemConstants.HTTP_PATH.JOB_STOP;


@SilenceEndPoint
@Validated
public class JobEndPoint {

    @Mapping(path = JOB_DISPATCH, method = RequestMethod.POST)
    public ApiResult<Boolean> dispatchJob(@Valid DispatchJobRequest dispatchJob) {

        try {
            JobContext jobContext = buildJobContext(dispatchJob);

            // 初始化调度信息（日志上报LogUtil）
            initLogContext(jobContext);

            if (Objects.nonNull(dispatchJob.getRetryCount()) && dispatchJob.getRetryCount() > 0) {
                SilenceJobLog.REMOTE.info("任务执行/调度失败执行重试. 重试次数:[{}]",
                        dispatchJob.getRetryCount());
            }

            if (!ExecutorType.JAVA.getValue().equals(dispatchJob.getExecutorType())) {

                SilenceJobLog.REMOTE.error("不支持非Java类型的执行器. executorType:[{}]", dispatchJob.getExecutorType());
                return new ApiResult<>("不支持非Java类型的执行器", Boolean.FALSE);
            }

            JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(jobContext.getExecutorInfo());
            if (Objects.isNull(jobExecutorInfo)) {
                SilenceJobLog.REMOTE.error("执行器配置有误. executorInfo:[{}]", dispatchJob.getExecutorInfo());
                return new ApiResult<>("执行器配置有误", Boolean.FALSE);
            }

            // 选择执行器
            Object executor = jobExecutorInfo.getExecutor();
            IJobExecutor jobExecutor;
            if (IJobExecutor.class.isAssignableFrom(executor.getClass())) {
                if (JobTaskType.MAP.equals(jobContext.getTaskType())) {
                    jobExecutor = (AbstractMapExecutor) executor;
                } else if (JobTaskType.MAP_REDUCE.equals( jobContext.getTaskType())) {
                    jobExecutor = (AbstractMapReduceExecutor) executor;
                } else {
                    jobExecutor = (AbstractJobExecutor) executor;
                }
            } else {
                if (JobTaskType.MAP.equals( jobContext.getTaskType())) {
                    jobExecutor = SilenceSpringContext.getBeanByType(AnnotationMapJobExecutor.class);
                } else if (JobTaskType.MAP_REDUCE.equals(jobContext.getTaskType())) {
                    jobExecutor = SilenceSpringContext.getBeanByType(AnnotationMapReduceJobExecutor.class);
                } else {
                    jobExecutor = SilenceSpringContext.getBeanByType(AnnotationJobExecutor.class);
                }
            }

            SilenceJobLog.REMOTE.info("批次:[{}] 任务调度成功. ", dispatchJob.getTaskBatchId());

            jobExecutor.jobExecute(jobContext);

        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("客户端发生非预期异常. taskBatchId:[{}]", dispatchJob.getTaskBatchId());
            throw e;
        } finally {
            SilenceJobLogManager.removeLogMeta();
        }

        return ApiResult.success(Boolean.TRUE);
    }

    private void initLogContext(JobContext jobContext) {
        JobLogMeta logMeta = new JobLogMeta();
        logMeta.setNamespaceId(jobContext.getNamespaceId());
        logMeta.setTaskId(jobContext.getTaskId());
        logMeta.setGroupName(jobContext.getGroupName());
        logMeta.setJobId(jobContext.getJobId());
        logMeta.setTaskBatchId(jobContext.getTaskBatchId());
        SilenceJobLogManager.initLogInfo(logMeta, LogTypeEnum.JOB);
    }


    private static JobContext buildJobContext(DispatchJobRequest dispatchJob) {
        JobContext jobContext = new JobContext();
        jobContext.setJobId(dispatchJob.getJobId());
        jobContext.setShardingTotal(dispatchJob.getShardingTotal());
        jobContext.setShardingIndex(dispatchJob.getShardingIndex());
        jobContext.setNamespaceId(dispatchJob.getNamespaceId());
        jobContext.setTaskId(dispatchJob.getTaskId());
        jobContext.setTaskBatchId(dispatchJob.getTaskBatchId());
        jobContext.setGroupName(dispatchJob.getGroupName());
        jobContext.setExecutorInfo(dispatchJob.getExecutorInfo());
        jobContext.setParallelNum(dispatchJob.getParallelNum());
        jobContext.setTaskType(EnumValueFactory.getRequired(JobTaskType.class, dispatchJob.getTaskType()));
        jobContext.setExecutorTimeout(dispatchJob.getExecutorTimeout());
        jobContext.setWorkflowNodeId(dispatchJob.getWorkflowNodeId());
        jobContext.setWorkflowTaskBatchId(dispatchJob.getWorkflowTaskBatchId());
        jobContext.setRetryStatus(dispatchJob.getRetryStatus());
        jobContext.setRetryScene(dispatchJob.getRetryScene());
        jobContext.setTaskName(dispatchJob.getTaskName());
        jobContext.setMrStage(EnumValueFactory.getRequired(MapReduceStage.class, dispatchJob.getMrStage()));

        if (StrUtil.isNotBlank(dispatchJob.getArgsStr())) {
            try {
                jobContext.setJobArgsHolder(JSON.parseObject(dispatchJob.getArgsStr(), JobArgsHolder.class));
            } catch (Exception e) {
                SilenceJobLog.REMOTE.warn("argsStr parse error", e);
                JobArgsHolder jobArgsHolder = new JobArgsHolder();
                jobArgsHolder.setJobParams(dispatchJob.getArgsStr());
                jobContext.setJobArgsHolder(jobArgsHolder);
            }
        } else {
            // 没有数据给个空对象，方便后面取参数
            jobContext.setJobArgsHolder(new JobArgsHolder());
        }

        String wfContext = dispatchJob.getWfContext();
        if (StrUtil.isNotBlank(wfContext)) {
            try {
                jobContext.setWfContext(JsonUtils.parseConcurrentHashMap(wfContext));
            } catch (Exception e) {
                SilenceJobLog.REMOTE.warn("workflow context parse error", e);
            }
        } else {
            jobContext.setWfContext(Maps.newConcurrentMap());
        }

        return jobContext;
    }

    @Mapping(path = JOB_STOP, method = RequestMethod.POST)
    public ApiResult<Boolean> stopJob(@Valid StopJobDTO interruptJob) {

        ThreadPoolExecutor threadPool = ThreadPoolCache.getThreadPool(interruptJob.getTaskBatchId());
        if (Objects.isNull(threadPool) || threadPool.isShutdown() || threadPool.isTerminated()) {
            return new ApiResult<>(Boolean.TRUE);
        }

        ThreadPoolCache.stopThreadPool(interruptJob.getTaskBatchId());
        return new ApiResult<>(threadPool.isShutdown() || threadPool.isTerminated());
    }
}
