package com.old.silence.job.client.core.dto;



import java.time.Instant;


public class JobResponseVO {

    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 执行方法参数
     */
    private String argsStr;

    /**
     * 扩展字段
     */
    private String extAttrs;

    /**
     * 下次触发时间
     */
    private Instant nextTriggerAt;

    /**
     * 重试状态 0、关闭、1、开启
     */
    private Boolean jobStatus;

    /**
     * 执行器路由策略
     */
    private Integer routeKey;

    /**
     * 执行器类型 1、Java
     */
    private Integer executorType;

    /**
     * 执行器名称
     */
    private String executorInfo;

    /**
     * 触发类型 1.CRON 表达式 2. 固定时间
     */
    private Integer triggerType;

    /**
     * 间隔时长
     */
    private String triggerInterval;

    /**
     * 阻塞策略 1、丢弃 2、覆盖 3、并行
     */
    private Integer blockStrategy;

    /**
     * 任务执行超时时间，单位秒
     */
    private Integer executorTimeout;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 重试间隔(s)
     */
    private Integer retryInterval;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 并行数
     */
    private Integer parallelNum;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Instant createdDate;

    /**
     * 修改时间
     */
    private Instant updatedDate;

}
