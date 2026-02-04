package com.old.silence.job.client.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.core.dto.RequestAddOrUpdateJobDTO;
import com.old.silence.job.common.enums.TriggerType;
import com.old.silence.job.client.common.exception.SilenceJobClientException;
import com.old.silence.job.common.enums.AllocationAlgorithmEnum;
import com.old.silence.job.common.enums.ExecutorType;
import com.old.silence.job.common.enums.JobArgsType;
import com.old.silence.job.common.enums.JobBlockStrategy;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.util.JsonUtils;



import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static com.old.silence.job.common.enums.TriggerType.SCHEDULED_TIME;
import static com.old.silence.job.common.enums.TriggerType.WORK_FLOW;


public abstract class AbstractParamsHandler<H, R> extends AbstractRequestHandler<R> {
    protected static final String SHARD_NUM = "shardNum";
    private final RequestAddOrUpdateJobDTO reqDTO;
    private H r;

    public AbstractParamsHandler(JobTaskType taskType) {
        this.reqDTO = new RequestAddOrUpdateJobDTO();
        // 默认创建就开启
        reqDTO.setJobStatus(true);
        // 设置任务类型
        reqDTO.setTaskType(taskType);
        // 默认java
        reqDTO.setExecutorType(ExecutorType.JAVA);
    }

    protected H setId(BigInteger id) {
        reqDTO.setId(id);
        return r;
    }

    /**
     * 修改时会直接覆盖之前的任务参数
     * 修改参数
     *
     * @param argsStr 请求参数
     * @return
     */
    private H setArgsStr(Map<String, Object> argsStr) {
        Map<String, Object> args = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getArgsStr())) {
            args = JsonUtils.parseHashMap(reqDTO.getArgsStr());
        }
        args.putAll(argsStr);
        reqDTO.setArgsStr(JSON.toJSONString(args));
        reqDTO.setArgsType(JobArgsType.JSON);
        return r;
    }

    /**
     * 修改Reduce的分片数
     * 只允许MAP_REDUCE设置
     *
     * @param shardNum
     * @return
     */
    protected H setShardNum(Integer shardNum) {
        // 设置分片
        if (shardNum != null) {
            Map<String, Object> map = new HashMap<>(1);
            map.put(SHARD_NUM, shardNum);
            setArgsStr(map);
        }
        return r;
    }

    /**
     * 设置任务名
     *
     * @param jobName 任务名
     * @return
     */
    public H setJobName(String jobName) {
        reqDTO.setJobName(jobName);
        return r;
    }

    /**
     * 添加参数，可支持多次添加
     * 静态分片不可使用该方法
     *
     * @param argsKey   参数名
     * @param argsValue 参数值
     */
    protected H addArgsStr(String argsKey, Object argsValue) {
        Map<String, Object> map = new HashMap<>();
        if (StrUtil.isNotBlank(reqDTO.getArgsStr())) {
            map = JsonUtils.parseHashMap(reqDTO.getArgsStr());
        }
        map.put(argsKey, argsValue);
        reqDTO.setArgsStr(JSON.toJSONString(map));
        reqDTO.setArgsType(JobArgsType.JSON);
        return r;
    }

    /**
     * 添加静态分片相关参数
     *
     * @param shardingValue 分片参数
     * @return r
     */
    protected H addShardingArgs(String... shardingValue) {
        reqDTO.setArgsStr(JSON.toJSONString(shardingValue));
        reqDTO.setArgsType(JobArgsType.TEXT);
        return r;
    }

    /**
     * 设置路由
     *
     * @param algorithmEnum 路由算法
     * @return r
     */
    protected H setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        reqDTO.setRouteKey(algorithmEnum.getType());
        return r;
    }

    /**
     * 设置执行器信息
     *
     * @param executorInfo 执行器信息
     * @return r
     */
    public H setExecutorInfo(String executorInfo) {
        reqDTO.setExecutorInfo(executorInfo);
        return r;
    }

    /**
     * 设置调度类型
     *
     * @param triggerType 触发类型
     * @return r
     */
    public H setTriggerType(TriggerType triggerType) {
        reqDTO.setTriggerType(triggerType);
        return r;
    }

    /**
     * 设置触发间隔；
     * 单位：秒
     * 注意: 此方法必须满足【triggerType==SCHEDULED_TIME】
     *
     * @param triggerInterval 触发间隔
     * @return r
     */
    public H setTriggerInterval(Integer triggerInterval) {
        Assert.isTrue(reqDTO.getTriggerType().equals(SCHEDULED_TIME),
                () -> new SilenceJobClientException("此方法只限制固定时间使用"));
        setTriggerInterval(String.valueOf(triggerInterval));
        return r;
    }

    /**
     * 设置触发间隔；
     * 单位：秒
     * 工作流无需配置
     *
     * @param triggerInterval 触发间隔
     * @return r
     */
    public H setTriggerInterval(String triggerInterval) {
        // 若是工作流则没有调度时间
        Assert.isFalse(reqDTO.getTriggerType().equals(WORK_FLOW),
                () -> new SilenceJobClientException("工作流无需配置"));
        reqDTO.setTriggerInterval(triggerInterval);
        return r;
    }

    /**
     * 设置阻塞策略
     *
     * @param blockStrategy 阻塞策略
     * @return r
     */
    public H setBlockStrategy(JobBlockStrategy blockStrategy) {
        reqDTO.setBlockStrategy(blockStrategy);
        return r;
    }

    /**
     * 设置执行器超时时间
     *
     * @param executorTimeout 超时时间(单位:秒)
     * @return r
     */
    public H setExecutorTimeout(Integer executorTimeout) {
        reqDTO.setExecutorTimeout(executorTimeout);
        return r;
    }

    /**
     * 设置任务最大重试次数
     *
     * @param maxRetryTimes 最大超时时间
     * @return r
     */
    public H setMaxRetryTimes(Integer maxRetryTimes) {
        reqDTO.setMaxRetryTimes(maxRetryTimes);
        return r;
    }

    /**
     * 设置重试间隔
     *
     * @param retryInterval 重试间隔
     * @return r
     */
    public H setRetryInterval(Integer retryInterval) {
        reqDTO.setRetryInterval(retryInterval);
        return r;
    }

    /**
     * 设置并发数量
     *
     * @param parallelNum 并发数量
     * @return r
     */
    protected H setParallelNum(Integer parallelNum) {
        reqDTO.setParallelNum(parallelNum);
        return r;
    }

    /**
     * 设置定时任务描述
     *
     * @param description 任务描述
     * @return r
     */
    public H setDescription(String description) {
        reqDTO.setDescription(description);
        return r;
    }


    public RequestAddOrUpdateJobDTO getReqDTO() {
        return reqDTO;
    }

    public H getR() {
        return r;
    }

    public void setR(H r) {
        this.r = r;
    }


}
