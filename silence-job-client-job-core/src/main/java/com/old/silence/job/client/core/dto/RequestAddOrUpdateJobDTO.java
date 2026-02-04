package com.old.silence.job.client.core.dto;

import com.old.silence.job.client.core.handler.add.Add;
import com.old.silence.job.client.core.handler.update.Update;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.TriggerType;
import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobTaskType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.math.BigInteger;


public class RequestAddOrUpdateJobDTO {

    @NotNull(message = "id 不能为空", groups = Update.class)
    private BigInteger id;

    /**
     * 名称
     */
    @NotBlank(message = "jobName 不能为空", groups = Add.class)
    private String jobName;

    /**
     * 重试状态 0、关闭、1、开启
     * {@link Boolean}
     */
    @NotNull(message = "jobStatus 不能为空", groups = Add.class)
    private Boolean jobStatus;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 参数类型 text/json
     */
    private JobArgsType argsType;

    /**
     * 执行器路由策略
     */
    @NotNull(message = "routeKey 不能为空", groups = Add.class)
    private Integer routeKey;

    /**
     * 执行器类型
     * {@link ExecutorType}
     */
    @NotNull(message = "executorType 不能为空", groups = Add.class)
    private ExecutorType executorType;

    /**
     * 执行器名称
     */
    @NotBlank(message = "executorInfo 不能为空", groups = Add.class)
    private String executorInfo;

    /**
     * 触发类型 2. 固定时间 3.CRON 表达式 99.工作流
     */
    @NotNull(message = "triggerType 不能为空", groups = Add.class)
    private TriggerType triggerType;

    /**
     * 间隔时长
     */
    @NotNull(message = "triggerInterval 不能为空", groups = Add.class)
    private String triggerInterval;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    @NotNull(message = "blockStrategy 不能为空", groups = Add.class)
    private JobBlockStrategy blockStrategy;

    /**
     * 任务执行超时时间，单位秒
     */
    @NotNull(message = "executorTimeout 不能为空", groups = Add.class)
    private Integer executorTimeout;

    /**
     * 最大重试次数
     */
    @NotNull(message = "maxRetryTimes 不能为空", groups = Add.class)
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    @NotNull(message = "retryInterval 不能为空", groups = Add.class)
    private Integer retryInterval;

    /**
     * 任务类型
     * {@link JobTaskType}
     */
    @NotNull(message = "taskType 不能为空", groups = Add.class)
    private JobTaskType taskType;

    /**
     * 并行数
     */
    @NotNull(message = "parallelNum 不能为空", groups = Add.class)
    private Integer parallelNum;

    /**
     * 描述
     */
    private String description;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Boolean getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(Boolean jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getArgsStr() {
        return argsStr;
    }

    public void setArgsStr(String argsStr) {
        this.argsStr = argsStr;
    }

    public JobArgsType getArgsType() {
        return argsType;
    }

    public void setArgsType(JobArgsType argsType) {
        this.argsType = argsType;
    }

    public Integer getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(Integer routeKey) {
        this.routeKey = routeKey;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public String getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(String executorInfo) {
        this.executorInfo = executorInfo;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerInterval() {
        return triggerInterval;
    }

    public void setTriggerInterval(String triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    public JobBlockStrategy getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(JobBlockStrategy blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public Integer getExecutorTimeout() {
        return executorTimeout;
    }

    public void setExecutorTimeout(Integer executorTimeout) {
        this.executorTimeout = executorTimeout;
    }

    public Integer getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(Integer maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public JobTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(JobTaskType taskType) {
        this.taskType = taskType;
    }

    public Integer getParallelNum() {
        return parallelNum;
    }

    public void setParallelNum(Integer parallelNum) {
        this.parallelNum = parallelNum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
