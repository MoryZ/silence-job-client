package com.old.silence.job.client.retry.core.executor;

import com.alibaba.fastjson2.JSON;

import com.old.silence.job.client.retry.core.RetryExecutor;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.client.retry.core.strategy.ExecutorAnnotationMethod;
import com.old.silence.job.client.retry.core.strategy.ExecutorMethod;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.old.silence.job.log.SilenceJobLog;




public abstract class AbstractRetryExecutor<BR, SR> implements RetryExecutor<BR, SR> {

    protected RetryerInfo retryerInfo;

    @Override
    public Object execute(Object... params) {
        return doExecute(params);
    }

    public Object doExecute(Object... params) {

        Class<? extends ExecutorMethod> retryMethodClass = retryerInfo.getExecutorMethod();
        if (retryMethodClass.isAssignableFrom(ExecutorAnnotationMethod.class)) {
            SilenceJobLog.LOCAL.debug("执行注解重试方法：{},参数为：{}", retryMethodClass.getName(), JSON.toJSONString(params));
            ExecutorAnnotationMethod retryAnnotationMethod = new ExecutorAnnotationMethod(retryerInfo);
            return retryAnnotationMethod.doExecute(params);
        } else {
            SilenceJobLog.LOCAL.debug("执行自定义重试方法：{},参数为：{}", retryMethodClass.getName(), JSON.toJSONString(params));
            ExecutorMethod executorMethod = SilenceSpringContext.getBeanByType(retryMethodClass);
            return executorMethod.doExecute(params);
        }
    }

    @Override
    public RetryerInfo getRetryerInfo() {
        return retryerInfo;
    }

}
