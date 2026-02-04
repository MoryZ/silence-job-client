package com.old.silence.job.client.core.executor;


import cn.hutool.core.date.DatePattern;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import com.alibaba.fastjson2.JSON;

import com.google.common.util.concurrent.FutureCallback;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.client.common.cache.GroupVersionCache;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.common.log.support.SilenceJobLogManager;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.client.core.cache.ThreadPoolCache;
import com.old.silence.job.client.core.client.JobNettyClient;
import com.old.silence.job.client.core.log.JobLogMeta;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.client.dto.request.DispatchJobResultRequest;
import com.old.silence.job.common.dto.ConfigDTO;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.common.alarm.AlarmContext;
import com.old.silence.job.common.alarm.SilenceJobAlarmFactory;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.common.enums.JobNotifyScene;
import com.old.silence.job.common.enums.JobTaskStatus;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.model.JobContext;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.util.EnvironmentUtils;
import com.old.silence.job.common.util.NetUtil;



public class JobExecutorFutureCallback implements FutureCallback<ExecuteResult> {

    private static final String TEXT_MESSAGE_FORMATTER =
            "<font face=\"微软雅黑\" color=#ff0000 size=4>{}环境 定时任务上报异常</font> \\s"+
            "> IP:{}   \\s"+
            "> 空间ID:{}  \\s"+
            "> 名称:{}   \\s"+
            "> 时间:{}   \\s"+
            "> 异常:{} \\s"+
            "\\s";

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, SilenceJobRpcResult>newBuilder()
            .client(JobNettyClient.class)
            .callback(nettyResult -> {
                if (nettyResult.getCode() == 500) {
                    sendMessage(nettyResult.getMessage());
                }
                SilenceJobLog.LOCAL.debug("Job execute result report successfully requestId:[{}]",
                        nettyResult.getReqId());
            }).build();

    private final JobContext jobContext;

    public JobExecutorFutureCallback(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    @Override
    public void onSuccess(ExecuteResult result) {

        try {
            // 初始化调度信息（日志上报LogUtil）
            initLogContext();

            // 上报执行成功
            SilenceJobLog.REMOTE.info("任务执行成功 taskBatchId:[{}] [{}]", jobContext.getTaskBatchId(), JSON.toJSONString(result));

            if (Objects.isNull(result)) {
                result = ExecuteResult.success();
            }

            JobTaskStatus taskStatus;
            if (result.getStatus() == 500) {
                taskStatus = JobTaskStatus.FAIL;
            } else {
                taskStatus = JobTaskStatus.SUCCESS;
            }

            CLIENT.dispatchResult(buildDispatchJobResultRequest(result, taskStatus));
        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("执行结果上报异常.[{}]", jobContext.getTaskId(), e);
            sendMessage(e.getMessage());
        } finally {
            SilenceJobLogManager.removeLogMeta();
            stopThreadPool();
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (t instanceof CancellationException) {
            SilenceJobLog.LOCAL.debug("任务已经被取消，不做状态回传");
            return;
        }
        ExecuteResult failure = ExecuteResult.failure();
        try {
            // 初始化调度信息（日志上报LogUtil）
            initLogContext();

            // 上报执行失败
            SilenceJobLog.REMOTE.error("任务执行失败 taskBatchId:[{}]", jobContext.getTaskBatchId(), t);
            failure.setMessage(t.getMessage());

            CLIENT.dispatchResult(
                    buildDispatchJobResultRequest(failure, JobTaskStatus.FAIL)
            );
        } catch (Exception e) {
            SilenceJobLog.REMOTE.error("执行结果上报异常.[{}]", jobContext.getTaskId(), e);
            sendMessage(e.getMessage());
        } finally {
            SilenceJobLogManager.removeLogMeta();
            stopThreadPool();
        }
    }

    private void initLogContext() {
        JobLogMeta logMeta = new JobLogMeta();
        logMeta.setNamespaceId(jobContext.getNamespaceId());
        logMeta.setTaskId(jobContext.getTaskId());
        logMeta.setGroupName(jobContext.getGroupName());
        logMeta.setJobId(jobContext.getJobId());
        logMeta.setTaskBatchId(jobContext.getTaskBatchId());
        SilenceJobLogManager.initLogInfo(logMeta, LogTypeEnum.JOB);
    }

    private void stopThreadPool() {
        if (jobContext.getTaskType().equals(JobTaskType.CLUSTER)) {
            ThreadPoolCache.stopThreadPool(jobContext.getTaskBatchId());
        }
    }

    private DispatchJobResultRequest buildDispatchJobResultRequest(ExecuteResult executeResult, JobTaskStatus status) {
        DispatchJobResultRequest dispatchJobRequest = new DispatchJobResultRequest();
        dispatchJobRequest.setTaskBatchId(jobContext.getTaskBatchId());
        dispatchJobRequest.setGroupName(jobContext.getGroupName());
        dispatchJobRequest.setJobId(jobContext.getJobId());
        dispatchJobRequest.setTaskId(jobContext.getTaskId());
        dispatchJobRequest.setWorkflowTaskBatchId(jobContext.getWorkflowTaskBatchId());
        dispatchJobRequest.setWorkflowNodeId(jobContext.getWorkflowNodeId());
        dispatchJobRequest.setTaskBatchId(jobContext.getTaskBatchId());
        dispatchJobRequest.setTaskId(jobContext.getTaskId());
        dispatchJobRequest.setTaskType(jobContext.getTaskType());
        dispatchJobRequest.setExecuteResult(executeResult);
        dispatchJobRequest.setTaskStatus(status);
        dispatchJobRequest.setRetryStatus(jobContext.getRetryStatus());
        dispatchJobRequest.setRetryScene(jobContext.getRetryScene());
        // 传递变更后的上下文
        if (CollectionUtils.isNotEmpty(jobContext.getChangeWfContext())) {
            dispatchJobRequest.setWfContext(JSON.toJSONString(jobContext.getChangeWfContext()));
        }

        return dispatchJobRequest;
    }

    private static void sendMessage(String message) {

        try {
            SilenceJobProperties silenceJobProperties = SilenceSpringContext.getBean(SilenceJobProperties.class);
            if (Objects.isNull(silenceJobProperties)) {
                return;
            }
            ConfigDTO.Notify notify = GroupVersionCache.getJobNotifyAttribute(
                    JobNotifyScene.JOB_CLIENT_ERROR);
            if (Objects.nonNull(notify)) {
                List<ConfigDTO.Notify.Recipient> recipients = Optional.ofNullable(notify.getRecipients()).orElse(Collections.emptyList());
                for (ConfigDTO.Notify.Recipient recipient : recipients) {
                    AlarmContext context = AlarmContext.build()
                            .text(TEXT_MESSAGE_FORMATTER,
                                    EnvironmentUtils.getActiveProfile(),
                                    NetUtil.getLocalIpStr(),
                                    silenceJobProperties.getNamespace(),
                                    silenceJobProperties.getGroup(),
                                    Instant.now().atZone(ZoneId.systemDefault()).format(DatePattern.NORM_DATETIME_FORMATTER),
                                    message)
                            .title("定时任务执行结果上报异常:[{}]", silenceJobProperties.getGroup())
                            .notifyAttribute(recipient.getNotifyAttribute());

                    Optional.ofNullable(SilenceJobAlarmFactory.getAlarmType(recipient.getNotifyType())).ifPresent(alarm -> alarm.asyncSendMessage(context));
                }
            }
        } catch (Exception e1) {
            SilenceJobLog.LOCAL.error("Client failed to send component exception alert.", e1);
        }
    }
}
