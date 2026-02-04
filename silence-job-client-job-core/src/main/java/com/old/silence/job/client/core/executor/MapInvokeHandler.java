package com.old.silence.job.client.core.executor;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.client.common.rpc.client.RequestBuilder;
import com.old.silence.job.client.core.client.JobNettyClient;
import com.old.silence.job.common.client.dto.ExecuteResult;
import com.old.silence.job.common.client.dto.request.MapTaskRequest;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.exception.SilenceJobMapReduceException;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.model.JobContext;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public final class MapInvokeHandler implements InvocationHandler {

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, SilenceJobRpcResult>newBuilder()
        .client(JobNettyClient.class)
        .async(Boolean.FALSE)
        .build();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return doMap((List<Object>) args[0], (String) args[1]);
    }

    public ExecuteResult doMap(List<Object> taskList, String nextTaskName) {

        if (StrUtil.isBlank(nextTaskName)) {
            throw new SilenceJobMapReduceException("The next task name can not blank or null {}", nextTaskName);
        }

        if (CollectionUtils.isEmpty(taskList)) {
            throw new SilenceJobMapReduceException("The task list can not empty {}", nextTaskName);
        }

        // 超过200提醒用户注意分片数量过多
        if (taskList.size() > 200) {
            SilenceJobLog.LOCAL.warn("[{}] map task size is too large, network maybe overload... please try to split the tasks.", nextTaskName);
        }

        // 超过500强制禁止分片
        if (taskList.size() > 500) {
            throw new SilenceJobMapReduceException("[{}] map task size is too large, network maybe overload... please try to split the tasks.", nextTaskName);
        }

        // taskName 任务命名和根任务名或者最终任务名称一致导致的问题（无限生成子任务或者直接失败）
        if (SystemConstants.ROOT_MAP.equals(nextTaskName)) {
            throw new SilenceJobMapReduceException("The Next taskName can not be {}", SystemConstants.ROOT_MAP);
        }

        // 使用ThreadLocal传递数据
        JobContext jobContext = JobContextManager.getJobContext();
        Assert.notNull(jobContext, () -> new SilenceJobMapReduceException("job context is null"));

        // 1. 构造请求
        MapTaskRequest mapTaskRequest = new MapTaskRequest();
        mapTaskRequest.setJobId(jobContext.getJobId());
        mapTaskRequest.setTaskBatchId(jobContext.getTaskBatchId());
        mapTaskRequest.setTaskName(nextTaskName);
        mapTaskRequest.setSubTask(taskList);
        mapTaskRequest.setParentId(jobContext.getTaskId());
        mapTaskRequest.setWorkflowTaskBatchId(jobContext.getWorkflowTaskBatchId());
        mapTaskRequest.setWorkflowNodeId(jobContext.getWorkflowNodeId());
        Map<String, Object> changeWfContext = jobContext.getChangeWfContext();
        if (Objects.nonNull(changeWfContext)) {
            mapTaskRequest.setWfContext(JSON.toJSONString(changeWfContext));
        }

        // 2. 同步发送请求
        ApiResult<Boolean> result = CLIENT.batchReportMapTask(mapTaskRequest);
        if (Objects.equals(200, result.getCode()) || result.getData()) {
            SilenceJobLog.LOCAL.info("Map task create successfully!. taskName:[{}] TaskId:[{}] ", nextTaskName, jobContext.getTaskId());
        } else {
            throw new SilenceJobMapReduceException("map failed for task: " + nextTaskName);
        }

        return ExecuteResult.success();
    }


}
