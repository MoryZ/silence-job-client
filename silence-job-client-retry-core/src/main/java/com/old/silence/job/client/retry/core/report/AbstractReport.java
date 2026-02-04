package com.old.silence.job.client.retry.core.report;

import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import com.old.silence.job.client.common.config.SilenceJobProperties;
import com.old.silence.job.client.retry.core.IdempotentIdGenerate;
import com.old.silence.job.client.retry.core.Report;
import com.old.silence.job.client.retry.core.RetryArgSerializer;
import com.old.silence.job.client.retry.core.cache.RetryerInfoCache;
import com.old.silence.job.client.retry.core.exception.SilenceRetryClientException;
import com.old.silence.job.client.retry.core.intercepter.RetrySiteSnapshot;
import com.old.silence.job.client.retry.core.loader.SilenceRetrySpiLoader;
import com.old.silence.job.client.retry.core.retryer.RetryerInfo;
import com.old.silence.job.common.expression.ExpressionEngine;
import com.old.silence.job.common.model.IdempotentIdContext;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.log.SilenceJobLog;

import java.lang.reflect.Method;

/**
 * 上报抽象类
 *
 */

public abstract class AbstractReport implements Report {
    @Autowired
    protected SilenceJobProperties silenceJobProperties;

    @Override
    public boolean report(String scene, String targetClassName, Object[] params) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Assert.notNull(retryerInfo, () -> new SilenceRetryClientException("retryerInfo is null"));

        if (RetrySiteSnapshot.getStage().equals(RetrySiteSnapshot.EnumStage.REMOTE.getStage()) && !retryerInfo.isForceReport()) {
            SilenceJobLog.LOCAL.info("Successfully reported, no need to repeat reporting. scene:[{}] targetClassName:[{}] args:[{}]",
                    retryerInfo.getScene(), retryerInfo.getExecutorClassName(), params);
            return Boolean.TRUE;
        }

        return doReport(retryerInfo, params);
    }

    public abstract boolean doReport(RetryerInfo retryerInfo, Object[] params);

    /**
     * 构建上报任务对象
     *
     * @param scene           场景
     * @param targetClassName 执行对象
     * @param args            参数
     * @return RetryTaskDTO 上报服务端对象
     */
    protected RetryTaskDTO buildRetryTaskDTO(String scene, String targetClassName, Object[] args) {
        RetryerInfo retryerInfo = RetryerInfoCache.get(scene, targetClassName);
        Method executorMethod = retryerInfo.getMethod();

        RetryTaskDTO retryTaskDTO = new RetryTaskDTO();
        String idempotentId;
        try {
            Class<? extends IdempotentIdGenerate> idempotentIdGenerate = retryerInfo.getIdempotentIdGenerate();
            IdempotentIdGenerate generate = idempotentIdGenerate.getConstructor().newInstance();
            Method method = idempotentIdGenerate.getMethod("idGenerate", IdempotentIdContext.class);
            IdempotentIdContext idempotentIdContext = new IdempotentIdContext(scene, targetClassName, args, executorMethod.getName());
            idempotentId = (String) ReflectionUtils.invokeMethod(method, generate, idempotentIdContext);
        } catch (Exception exception) {
            SilenceJobLog.LOCAL.error("幂等id生成异常：{},{}", scene, args, exception);
            throw new SilenceRetryClientException("idempotentId生成异常：{},{}", scene, args);
        }

        RetryArgSerializer retryArgSerializer = SilenceRetrySpiLoader.loadRetryArgSerializer();

        String serialize = retryArgSerializer.serialize(args);
        retryTaskDTO.setIdempotentId(idempotentId);
        retryTaskDTO.setExecutorName(targetClassName);
        retryTaskDTO.setArgsStr(serialize);
        retryTaskDTO.setGroupName(silenceJobProperties.getGroup());
        retryTaskDTO.setSceneName(scene);

        String expression = retryerInfo.getBizNo();
        ExpressionEngine expressionEngine = SilenceRetrySpiLoader.loadExpressionEngine();
        retryTaskDTO.setBizNo((String) expressionEngine.eval(expression, args, executorMethod));
        return retryTaskDTO;
    }

}
